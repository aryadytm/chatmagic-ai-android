package dev.bytebooster.chatmagicai.ai.downloader

import android.content.Context
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import dev.bytebooster.chatmagicai.logDebug
import java.io.File

class FetchAiDownloader constructor(context: Context) {

    private val fetchConfiguration = FetchConfiguration.Builder(context)
        .setDownloadConcurrentLimit(1)
        .preAllocateFileOnCreation(false)
        .enableAutoStart(true)
        .createDownloadFileOnEnqueue(false)
        .enableRetryOnNetworkGain(true)
        .setNotificationManager(object : MyFetchNotificationManager(context) {

            override fun getFetchInstanceForNamespace(namespace: String): Fetch {
                return getFetch()
            }

            override fun getDownloadNotificationTitle(download: Download): String {
                return "Downloading AI Model..."
            }

            override fun shouldCancelNotification(downloadNotification: DownloadNotification): Boolean {
                return downloadNotification.isCompleted ||
                        downloadNotification.isRemoved ||
                        downloadNotification.isDeleted ||
                        downloadNotification.isFailed ||
                        downloadNotification.isCancelled
            }

        })
        .build()
    private val fetch = Fetch.Impl.getInstance(fetchConfiguration)
    private val filesDir = context.filesDir

    private var currentDownload: Download? = null
    private var onProgressFunc: (Download, Long, Long) -> Unit = { _, _, _ -> }
    private var onCompletedFunc: (Download) -> Unit = { }
    private var onErrorFunc: (Download, Error, Throwable) -> Unit = { _, _, _ -> }

    init {
        fetch.addListener(getListener())
    }

    fun downloadFile(url: String, notificationTitle: String): Long {
        val filename = URLUtil.guessFileName(url, null, MimeTypeMap.getFileExtensionFromUrl(url))
        val fileDestination = File(filesDir, filename).absolutePath

        val request = Request(url, fileDestination)
        request.priority = Priority.HIGH

        fetch.enqueue(request, { updatedRequest ->  }) { error -> error.throwable?.printStackTrace() }

        return request.id.toLong()
    }

    fun getCurrentDownload(): Download? {
        return currentDownload
    }

    fun cancelAndDelete(): Boolean {
        val id = currentDownload?.id ?: return false
        fetch.delete(id)
        finishDownload()
        return true
    }

    fun finishDownload(): Boolean {
        if (currentDownload == null) {
            return false
        }
        currentDownload = null
        return true
    }

    fun getFetch(): Fetch {
        return fetch
    }

    fun setListeners(
        onProgress: (Download, Long, Long) -> Unit,
        onCompleted: (Download) -> Unit,
        onError: (Download, Error, Throwable) -> Unit
    ) {
        onProgressFunc = onProgress
        onCompletedFunc = onCompleted
        onErrorFunc = onError
    }

    private fun getListener(): FetchListener {
        return object : FetchListener {
            override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
                logDebug("Queued Download: ${download.file} | ${download.id}")
                currentDownload = download
            }
            override fun onProgress(
                download: Download,
                etaInMilliSeconds: Long,
                downloadedBytesPerSecond: Long
            ) {
                // TODO: Show notification
                logDebug("Progress: ${download.progress} | ETA seconds: ${etaInMilliSeconds / 1000} | ${download.id} | ${download.file} | ${download.fileUri}")
                currentDownload = download
                onProgressFunc(download, etaInMilliSeconds, downloadedBytesPerSecond)
            }
            override fun onCompleted(download: Download) {
                // TODO: Show notification
                logDebug("Completed Download: ${download.file} | ${download.id}")
                onCompletedFunc(download)
            }
            override fun onPaused(download: Download) {
                logDebug("Paused Download: ${download.file} | ${download.id}")
            }
            override fun onResumed(download: Download) {
                logDebug("Resumed Download: ${download.file} | ${download.id}")
                currentDownload = download
            }
            override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
                logDebug("Started Download: ${download.file} | ${download.id}")
                currentDownload = download
            }
            override fun onWaitingNetwork(download: Download) {
                logDebug("Waiting Network Download: ${download.file} | ${download.id}")
            }
            override fun onAdded(download: Download) {
                logDebug("Added Download: ${download.file} | ${download.id}")
                currentDownload = download
            }
            override fun onCancelled(download: Download) {
                logDebug("Cancelled Download: ${download.file} | ${download.id}")
                onErrorFunc(download, Error.NONE, Exception("Download cancelled"))
            }
            override fun onRemoved(download: Download) {
                logDebug("Removed Download: ${download.file} | ${download.id}")
            }
            override fun onDeleted(download: Download) {
                logDebug("Deleted Download: ${download.file} | ${download.id}")
            }
            override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {
                logDebug("Download Block Update: ${download.file} | ${download.id}")
            }
            override fun onError(download: Download, error: Error, throwable: Throwable?) {
                throwable?.printStackTrace()
                logDebug("Error: $throwable")
                onErrorFunc(download, error, throwable ?: Exception("Download error (No throwable)"))
            }
        }
    }
}