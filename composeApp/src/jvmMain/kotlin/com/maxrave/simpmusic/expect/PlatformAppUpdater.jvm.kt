package com.maxrave.simpmusic.expect

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
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
            val downloadDir = File(System.getProperty("user.home"), "Downloads").takeIf { it.exists() }
                ?: File(System.getProperty("java.io.tmpdir"))
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
                connection.setRequestProperty("User-Agent", "SimpMusic-Desktop")

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
            val file = File(filePath)
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file)
            }
        }
    }
}
