package dev.bytebooster.chatmagicai.ai.downloader

interface AiDownloader {

    fun downloadFile(url: String, notificationTitle: String = "Downloading AI Model"): Long

    fun getDownloadInfo(id: Long): DownloadInfo

    fun cancel(downloadId: Long): Int

}