package dev.bytebooster.chatmagicai.ai

import android.content.Context
import dev.bytebooster.chatmagicai.R
import dev.bytebooster.chatmagicai.data.PrefManager
import dev.bytebooster.chatmagicai.data.RemoteConfigDatasource
import dev.bytebooster.chatmagicai.logDebug
import dev.bytebooster.chatmagicai.model.*
import javax.inject.Inject


class AiModelDatasource @Inject constructor(
    context: Context,
    private val remoteConfigDatasource: RemoteConfigDatasource,
)
{
    companion object {
        const val PREFNAME_AIMODELDATA = "chatmagicai"
        const val PREFKEY_USED_MODEL = "pref_used_model"
    }

    private val prefManager = PrefManager(context, PREFNAME_AIMODELDATA)
    private val resources = context.resources

    private lateinit var allModels: List<AiModel>
    private lateinit var allFormatters: Map<String, ChatFormatter>
    private lateinit var allModelChoices: AiModelMap

    fun isLoaded(): Boolean {
        return this::allModels.isInitialized
    }

    /**
     * Called after Firebase Remote Config finished fetching the latest data.
     */
    fun onLoadRemoteData() {
        allModels = remoteConfigDatasource.getAllModels().map {
            getModelWithDescriptor(it)
        }
        allFormatters = remoteConfigDatasource.getAllFormatters()
        allModelChoices = remoteConfigDatasource.getAiModelMaps()
    }

    fun getUsedModel(): AiModel? {
        val modelId = prefManager.getInt(PREFKEY_USED_MODEL, -1)
        return getModelById(modelId)
    }

    fun saveAsUsedModel(model: AiModel) {
        logDebug("Saving model as used: ${model.name}")
        prefManager.setInt(PREFKEY_USED_MODEL, model.id)
    }

    fun getInferenceModel(model: AiModel): InferenceAiModel {
        val formatter = allFormatters[model.formatter]!!
        return InferenceAiModel(model, formatter)
    }

    fun unuseModel() {
        prefManager.setInt(PREFKEY_USED_MODEL, -1)
    }

    fun getModelById(id: Int): AiModel? {
        return try {
            allModels.filter { it.id == id }[0]
        } catch (e: Exception) {
            null
        }
    }

    fun getModelByFilename(filename: String): AiModel? {
        return try {
            allModels.first { it.downloadUrl.contains(filename) }
        } catch (e: Exception) {
            null
        }
    }

    fun getStartingModels(): List<AiModel> {
        return listOf(getSmallModel(), getMediumModel())
    }

    fun getSmallModel(): AiModel {
        return getModelWithDescriptor(allModelChoices.small)
    }

    fun getMediumModel(): AiModel {
        return getModelWithDescriptor(allModelChoices.medium)
    }

    fun getLargeModel(): AiModel {
        return getModelWithDescriptor(allModelChoices.large)
    }

    fun getModelWithDescriptor(model: AiModel): AiModel {
        var name = ""
        var description = ""

        when (model.type) {
            MODEL_TYPE_SMALL -> {
                name = resources.getString(R.string.chatmagic_ai_small)
                description = resources.getString(R.string.desc_model_small)
            }
            MODEL_TYPE_MEDIUM -> {
                name = resources.getString(R.string.chatmagic_ai_medium)
                description = resources.getString(R.string.desc_model_medium)
            }
            MODEL_TYPE_LARGE -> {
                name = resources.getString(R.string.chatmagic_ai_large)
                description = resources.getString(R.string.desc_model_large)
            }
            MODEL_TYPE_TEST -> {
                name = resources.getString(R.string.chatmagic_ai_test)
                description = resources.getString(R.string.desc_model_test)
            }
        }

        return model.copy(name = name, description = description)
    }

}