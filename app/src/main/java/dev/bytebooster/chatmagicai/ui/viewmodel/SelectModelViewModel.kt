package dev.bytebooster.chatmagicai.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.bytebooster.chatmagicai.MainActivity
import dev.bytebooster.chatmagicai.R
import dev.bytebooster.chatmagicai.ai.AiModelDatasource
import dev.bytebooster.chatmagicai.ai.AiModelManager
import dev.bytebooster.chatmagicai.ai.downloader.FetchAiDownloader
import dev.bytebooster.chatmagicai.logDebug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@HiltViewModel
class SelectModelViewModel @Inject constructor(
    private val aiModelManager: AiModelManager,
    private val aiModelSource: AiModelDatasource,
    private val aiDownloader: FetchAiDownloader,
    @ApplicationContext context: Context,
    ) : ViewModel()
{
    private val resources = context.resources
    private val downloadErrorToast = Toast.makeText(context, "Download error. Please restart the app.", Toast.LENGTH_LONG)

    private lateinit var _uiState: MutableStateFlow<SelectModelUiState>
    val uiState get() = _uiState.asStateFlow()

    init {
        aiDownloader.setListeners(
            onProgress = { download, eta, speed -> onProgress(download, eta, speed) },
            onCompleted = { it -> onCompleted(it) },
            onError = { download, error, throwable -> onError(download, error, throwable) }
        )
    }

    fun loadSelectModelPage() {
        val modelsList = aiModelSource.getStartingModels().toMutableList()
        var selectedModel = aiModelSource.getUsedModel()

        if (selectedModel == null) {
            // It must be a new user. Set default selected model to small.
            selectedModel = aiModelSource.getSmallModel()
        }
        else {
            var isUpdate = false

            val idxOfSelectedModelSize = modelsList.indexOfFirst { it.type == selectedModel!!.type }
            selectedModel = selectedModel.copy(name = selectedModel.name + " (Active)")

            // Show updated version of selected model "Model size (Update)"
            for (model in modelsList.toList()) {
                if (model.type == selectedModel.type && model.id != selectedModel.id) {

                    isUpdate = true

                    // There is an update of the selected model.
                    // First, add the selected old model to the list.
                    modelsList.add(idxOfSelectedModelSize, selectedModel)
                    // The selected model have an updated version.
                    val updatedModel = model.copy(
                        name = model.name + " (Update)",
                        description = resources.getString(R.string.the_updated_version_of_this_ai_model)
                    )
                    val idxOfUpdatedModelSize = modelsList.indexOfFirst { it.id == updatedModel.id }
                    modelsList[idxOfUpdatedModelSize] = updatedModel
                }
            }

            if (!isUpdate) {
                modelsList[idxOfSelectedModelSize] = selectedModel
            }
        }

        _uiState = MutableStateFlow(
            SelectModelUiState(
                startingModels = modelsList,
                selectedModelCard = selectedModel
            )
        )
    }

    fun loadDownloadModelPage() {
        // User finished selecting model by click "Continue" button.

        // Delete all failed, cancelled, and removed downloads.
        val fetch = aiDownloader.getFetch()
        fetch.deleteAllWithStatus(Status.CANCELLED)
        fetch.deleteAllWithStatus(Status.FAILED)
        fetch.deleteAllWithStatus(Status.REMOVED)

        val currentDownload = aiDownloader.getCurrentDownload()
        logDebug("Current download: ${currentDownload?.status?.name}")

        if (currentDownload != null) {
            // There is a download in progress. Watch the download progress.
            return
        }

        fetch.getDownloadsWithStatus(Status.COMPLETED) { downloads ->
            // If the model file is already downloaded, use that model instead. Else download the model.
            val selectedModel = uiState.value.selectedModelCard

            if (downloads.any { it.url == selectedModel.downloadUrl }) {
                // The model file exists. No need to download. Use that model instead.
                aiModelSource.saveAsUsedModel(selectedModel)
                _uiState.update { it.copy(downloadProgress = 1f) }
            }
            else {
                // The model file does not exist. Download the model.
                aiDownloader.downloadFile(selectedModel.downloadUrl, "Downloading ${selectedModel.name}")
            }
        }
    }

    fun updateUiState(newUiState: SelectModelUiState) {
        _uiState.update { newUiState }
    }

    fun askPostNotificationPermission(activity: MainActivity) {
        val toastAskNotification = Toast.makeText(activity, activity.getString(R.string.notification_is_needed), Toast.LENGTH_LONG)

        if (Build.VERSION.SDK_INT > 32) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                toastAskNotification.show()
                activity.requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun onProgress(download: Download, etaMillis: Long, downloadSpeed: Long) {
        try {
            val downloadingModel = aiModelSource.getModelByFilename(download.fileUri.lastPathSegment!!)!!
            _uiState.update {
                it.copy(
                    downloadProgress = download.progress.toFloat() / 100f,
                    selectedModelCard = downloadingModel
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onCompleted(download: Download) {
        val downloadingModel = aiModelSource.getModelByFilename(download.fileUri.lastPathSegment!!)!!
        aiModelSource.saveAsUsedModel(downloadingModel)
        aiDownloader.finishDownload()
        _uiState.update {
            it.copy(downloadProgress = 1f)
        }
    }

    private fun onError(download: Download, error: com.tonyodev.fetch2.Error, throwable: Throwable?) {
        aiDownloader.cancelAndDelete()
        downloadErrorToast.show()
        throw Exception("Download Failed: ${throwable?.message}")
    }

    override fun onCleared() {
        super.onCleared()
    }
}