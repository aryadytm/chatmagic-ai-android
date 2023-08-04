package dev.bytebooster.chatmagicai.ai.downloader

import android.content.Context

class AndroidAiDownloader constructor(
    private val context: Context
) {

//    private val downloadManager: DownloadManager = context.getSystemService(DownloadManager::class.java)
//
//    fun downloadFile(url: String, notificationTitle: String): Long {
//
//        val filename = URLUtil.guessFileName(url, null, MimeTypeMap.getFileExtensionFromUrl(url))
//
//        val request = DownloadManager.Request(url.toUri())
//            .setTitle(notificationTitle)
//            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
//            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename)
//            .setAllowedOverMetered(true)
//            .setAllowedOverRoaming(true)
//            .setRequiresCharging(false)
//            .setRequiresDeviceIdle(false)
//
//        return downloadManager.enqueue(request)
//    }
//
//    @SuppressLint("Range")
//    fun getDownloadInfo(downloadId: Long): DownloadInfo {
//        var progress = 0
//        var isDownloadFinished = false
//
//        val cursor: Cursor = downloadManager.query(
//            DownloadManager.Query().setFilterById(downloadId)
//        )
//
//        if (!cursor.moveToFirst()) {
//            logDebug("Download cursor is empty!")
//            return DownloadInfo(DownloadStatus.NOT_DOWNLOADING, 0)
//        }
//
//        val downloadStatus = when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
//            DownloadManager.STATUS_RUNNING -> {
//                val totalBytes = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
//                if (totalBytes > 0) {
//                    val downloadedBytes = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
//                    progress = ((downloadedBytes.toFloat() / totalBytes.toFloat()) * 100).toInt()
//                }
//                logDebug("Download running: $progress")
//                DownloadStatus.RUNNING
//            }
//            DownloadManager.STATUS_SUCCESSFUL -> {
//                logDebug("Download success")
//                progress = 100
//                isDownloadFinished = true
//                DownloadStatus.SUCCESS
//            }
//            DownloadManager.STATUS_PAUSED -> {
//                logDebug("Download paused")
//                DownloadStatus.PAUSED
//            }
//            DownloadManager.STATUS_PENDING -> {
//                logDebug("Download pending")
//                DownloadStatus.PENDING
//            }
//            DownloadManager.STATUS_FAILED -> {
//                logDebug("Download failed")
//                isDownloadFinished = true
//                DownloadStatus.FAILED
//            }
//            else -> {
//                DownloadStatus.NOT_DOWNLOADING
//            }
//        }
//
//        return DownloadInfo(downloadStatus, progress)
//    }
//
//    fun cancel(downloadId: Long): Int {
//        return downloadManager.remove(downloadId)
//    }

}