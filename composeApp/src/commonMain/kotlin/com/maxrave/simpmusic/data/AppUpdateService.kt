package com.maxrave.simpmusic.data

import com.maxrave.simpmusic.model.AppUpdateInfo
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

class AppUpdateService(
    private val client: HttpClient = HttpClient(CIO),
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getLatestRelease(): Result<AppUpdateInfo> =
        runCatching {
            fetchFromRepo("beyondbday69/SimpMusic").getOrElse {
                fetchFromRepo("maxrave-dev/SimpMusic").getOrThrow()
            }
        }

    private suspend fun fetchFromRepo(repo: String): Result<AppUpdateInfo> =
        runCatching {
            val response =
                client.get("https://api.github.com/repos/$repo/releases?per_page=10") {
                    header("User-Agent", "SimpMusic-App")
                    header("Accept", "application/vnd.github.v3+json")
                }
            if (!response.status.isSuccess()) {
                throw IllegalStateException("GitHub API returned status ${response.status}")
            }
            val text = response.bodyAsText()
            val array = json.parseToJsonElement(text).jsonArray
            if (array.isEmpty()) {
                throw IllegalStateException("No releases found on $repo")
            }

            var chosenRelease = array[0].jsonObject
            var chosenApkAsset =
                chosenRelease["assets"]?.jsonArray?.firstOrNull {
                    it.jsonObject["name"]?.jsonPrimitive?.contentOrNull?.endsWith(".apk", ignoreCase = true) == true
                }?.jsonObject

            for (item in array) {
                val rel = item.jsonObject
                val apk =
                    rel["assets"]?.jsonArray?.firstOrNull {
                        it.jsonObject["name"]?.jsonPrimitive?.contentOrNull?.endsWith(".apk", ignoreCase = true) == true
                    }?.jsonObject
                if (apk != null) {
                    chosenRelease = rel
                    chosenApkAsset = apk
                    break
                }
            }

            val tagName = chosenRelease["tag_name"]?.jsonPrimitive?.contentOrNull ?: ""
            val publishedAt = chosenRelease["published_at"]?.jsonPrimitive?.contentOrNull
            val body = chosenRelease["body"]?.jsonPrimitive?.contentOrNull ?: ""
            val downloadUrl =
                chosenApkAsset?.get("browser_download_url")?.jsonPrimitive?.contentOrNull
                    ?: chosenRelease["html_url"]?.jsonPrimitive?.contentOrNull
            val fileName =
                chosenApkAsset?.get("name")?.jsonPrimitive?.contentOrNull
                    ?: "SimpMusic-$tagName.apk"
            val apkSize = chosenApkAsset?.get("size")?.jsonPrimitive?.longOrNull

            AppUpdateInfo(
                tagName = tagName,
                releaseTime = publishedAt,
                body = body,
                downloadUrl = downloadUrl,
                fileName = fileName,
                apkSize = apkSize,
            )
        }
}
