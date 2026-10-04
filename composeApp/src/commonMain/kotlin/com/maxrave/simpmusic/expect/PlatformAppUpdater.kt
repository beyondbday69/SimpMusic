package com.maxrave.simpmusic.expect

expect object PlatformAppUpdater {
    suspend fun downloadUpdate(
        url: String,
        fileName: String,
        onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit,
    ): Result<String>

    fun installUpdate(filePath: String): Result<Unit>
}
