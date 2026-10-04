package com.maxrave.simpmusic.model

data class AppUpdateInfo(
    val tagName: String,
    val releaseTime: String?,
    val body: String,
    val downloadUrl: String? = null,
    val fileName: String? = null,
    val apkSize: Long? = null,
)
