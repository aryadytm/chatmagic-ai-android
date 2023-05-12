package dev.bytebooster.chatmagicai.ai

import android.content.Context
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import dev.bytebooster.chatmagicai.ai.downloader.FetchAiDownloader
import dev.bytebooster.chatmagicai.ai.textgen.TextGenLoader
import dev.bytebooster.chatmagicai.ai.textgen.TextGenerator
import dev.bytebooster.chatmagicai.logDebug
import dev.bytebooster.chatmagicai.model.AiModel
import dev.bytebooster.chatmagicai.model.LocalAiModel
import java.io.File
import javax.inject.Inject



class AiModelManager @Inject constructor(
    private val context: Context,
    private val loader: TextGenLoader,
    private val downloader: FetchAiDownloader,
    private val modelDatasource: AiModelDatasource,
    private val fetchAiDownloader: FetchAiDownloader
) {

    private val fetch = fetchAiDownloader.getFetch()

    companion object {
        const val DOWNLOAD_STATUS_EMPTY_CURSOR = -6969
        const val FILE_FORMAT_MODEL = ".model"
    }

    fun isExists(model: AiModel): Boolean {
        // Model file exists and matches size
        val modelFile = getModelFile(model)
        return modelFile.exists()
    }

    fun delete(model: AiModel): Boolean {
        val modelFile = getModelFile(model)
        val usedModel = modelDatasource.getUsedModel()

        if (usedModel != null && usedModel.id == model.id) {
            modelDatasource.unuseModel()
        }

        modelFile.delete()

        fetch.getDownloads {downloads ->
            downloads.forEach {
                if (it.url == model.downloadUrl) {
                    fetch.delete(it.id)
                }
            }
        }

        return true
    }

    fun getDownloadedModels(): List<LocalAiModel> {
        val folder = context.filesDir
        val files = folder.listFiles()
        val localModels = mutableListOf<LocalAiModel>()

        if (files == null) {
            return emptyList()
        }

        for (file in files) {
            if (file.isFile && file.name.endsWith(FILE_FORMAT_MODEL)) {

                try {
                    localModels.add(
                        LocalAiModel(file = file, model = modelDatasource.getModelByFilename(file.name)!!)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    logDebug("Error get downloaded model: ${file.name}")
                }

            }
        }

        return localModels
    }


    fun loadModel(model: AiModel): TextGenerator {
        return loader.loadTextGenModel(getModelFile(model), model.type)
    }

    fun unload(textgen: TextGenerator): Boolean {
        textgen.unload()
        return true
    }

    private fun getModelFile(model: AiModel): File {
        val modelFileName = URLUtil.guessFileName(
            model.downloadUrl, null, MimeTypeMap.getFileExtensionFromUrl(model.downloadUrl))
        return File(context.filesDir, modelFileName)
    }

}