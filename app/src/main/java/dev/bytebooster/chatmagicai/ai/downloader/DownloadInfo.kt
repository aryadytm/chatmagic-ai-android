package dev.bytebooster.chatmagicai.ai.downloader

import com.tonyodev.fetch2.Status


enum class DownloadStatus {
    RUNNING,
    SUCCESS,
    PAUSED,
    PENDING,
    FAILED,
    NOT_DOWNLOADING
}

data class DownloadInfo(
    val status: Status,
    val progress: Int,
    val fileDestination: String,
    val url: String
)