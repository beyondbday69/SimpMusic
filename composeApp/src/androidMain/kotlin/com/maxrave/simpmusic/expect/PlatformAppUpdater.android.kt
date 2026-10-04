package com.maxrave.simpmusic.expect

import android.content.Intent
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.mp.KoinPlatform.getKoin
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

actual object PlatformAppUpdater {
    actual suspend fun downloadUpdate(
        url: String,
        fileName: String,
        onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val context: AppCompatActivity = getKoin().get()
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: context.cacheDir
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            val destinationFile = File(downloadDir, fileName)
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            var currentUrl = url
            var redirectCount = 0
            var connection: HttpURLConnection

            while (true) {
                val u = URL(currentUrl)
                connection = u.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.setRequestProperty("User-Agent", "SimpMusic-Android")

                val code = connection.responseCode
                if (code in 300..399) {
                    val loc = connection.getHeaderField("Location")
                        ?: throw IllegalStateException("Redirect without Location header")
                    currentUrl = loc
                    redirectCount++
                    if (redirectCount > 8) {
                        throw IllegalStateException("Too many redirects: $redirectCount")
                    }
                    continue
                }
                break
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw IllegalStateException("HTTP error $responseCode: ${connection.responseMessage}")
            }

            val totalBytes = connection.contentLengthLong.takeIf { it > 0 }
                ?: connection.contentLength.toLong()

            var bytesDownloaded = 0L
            destinationFile.outputStream().use { output ->
                connection.inputStream.use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        bytesDownloaded += bytesRead
                        onProgress(bytesDownloaded, totalBytes)
                    }
                    output.flush()
                }
            }

            destinationFile.absolutePath
        }
    }

    actual fun installUpdate(filePath: String): Result<Unit> {
        return runCatching {
            val context: AppCompatActivity = getKoin().get()
            val file = File(filePath)
            if (!file.exists()) {
                throw IllegalStateException("Downloaded APK file not found: $filePath")
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.FileProvider",
                file,
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(installIntent)
        }
    }
}
