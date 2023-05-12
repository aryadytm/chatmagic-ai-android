package dev.bytebooster.chatmagicai.data

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dev.bytebooster.chatmagicai.ai.ChatFormatter
import dev.bytebooster.chatmagicai.ai.ConvPairFormatter
import dev.bytebooster.chatmagicai.ai.TurnBasedFormatter
import dev.bytebooster.chatmagicai.logDebug
import dev.bytebooster.chatmagicai.model.AiModel
import dev.bytebooster.chatmagicai.model.AiModelMap
import dev.bytebooster.chatmagicai.model.GenerationParameters
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject


class RemoteConfigDatasource @Inject constructor(private val remoteConfig: FirebaseRemoteConfig) {

    companion object {
        const val REMOTE_KEY_ALL_MODELS = "all_models"
        const val REMOTE_KEY_ALL_FORMATTERS = "all_formatters"
        const val REMOTE_KEY_MODEL_MAPS = "model_maps"
        const val REMOTE_KEY_GENERATION_PARAMETERS = "generation_parameters"
        const val REMOTE_KEY_URL_CONTACT = "url_contact"
        const val REMOTE_KEY_URL_FEEDBACK = "url_feedback"
        const val REMOTE_KEY_URL_PRIVACY_POLICY = "url_privacy_policy"
        const val REMOTE_KEY_URL_TERMS_OF_SERVICE = "url_terms_of_service"
        const val REMOTE_KEY_URL_LEGAL = "url_legal"
        const val REMOTE_KEY_URL_ANALYTICS = "url_analytics"
    }

    val contactUrl: String get() { return remoteConfig.getString(REMOTE_KEY_URL_CONTACT) }
    val feedbackUrl: String get() { return remoteConfig.getString(REMOTE_KEY_URL_FEEDBACK) }
    val privacyPolicyUrl: String get() { return remoteConfig.getString(REMOTE_KEY_URL_PRIVACY_POLICY) }
    val termsOfServiceUrl: String get() { return remoteConfig.getString(REMOTE_KEY_URL_TERMS_OF_SERVICE) }
    val legalUrl: String get() { return remoteConfig.getString(REMOTE_KEY_URL_LEGAL) }

    fun getAllModels(): List<AiModel> {
        val models = mutableListOf<AiModel>()
        val json = remoteConfig.getString(REMOTE_KEY_ALL_MODELS)
        val jsonArray = JSONArray(json)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val id = jsonObject.getInt("id")
            val type = jsonObject.getString("type")
            val downloadUrl = jsonObject.getString("downloadUrl")
            val formatter = jsonObject.getString("formatter")
            val name = jsonObject.getString("name")
            val description = jsonObject.getString("description")
            val size = jsonObject.getLong("size")
            models.add(AiModel(id, name, type, description, downloadUrl, size, formatter))
        }
        return models
    }

    fun getAllFormatters(): Map<String, ChatFormatter> {
        val formatters = mutableMapOf<String, ChatFormatter>()
        val json = remoteConfig.getString(REMOTE_KEY_ALL_FORMATTERS)
        val jsonObject = JSONObject(json)

        logDebug(json)

        for (key in jsonObject.keys()) {
            val formatterJson = jsonObject.getJSONObject(key)
            val formatter = when (val name = formatterJson.getString("name")) {
                "ConvPairFormatter" -> {
                    val maxTokens = formatterJson.getInt("maxTokens")
                    val maxMessages = formatterJson.getInt("maxMessages")
                    val userPrefix = formatterJson.getString("userPrefix")
                    val botPrefix = formatterJson.getString("botPrefix")
                    val turnSeparator = formatterJson.getString("userBotSeparator")
                    val trim = formatterJson.getBoolean("trim")
                    ConvPairFormatter(
                        maxTokens=maxTokens, maxMessages=maxMessages, userPrefix=userPrefix, botPrefix=botPrefix, turnSeparator=turnSeparator, trim=trim
                    )
                }
                "TurnBasedFormatter" -> {
                    val maxTokens = formatterJson.getInt("maxTokens")
                    val maxMessages = formatterJson.getInt("maxMessages")
                    val turnToken = formatterJson.getString("turnToken")
                    val trim = formatterJson.getBoolean("trim")
                    TurnBasedFormatter(
                        maxTokens=maxTokens, maxMessages=maxMessages, turnToken=turnToken, trim=trim
                    )
                }
                else -> throw IllegalArgumentException("Unknown formatter name: $name")
            }
            formatters[key] = formatter
        }
        return formatters
    }

    fun getAiModelMaps(): AiModelMap {
        val modelMapJson = remoteConfig.getString(REMOTE_KEY_MODEL_MAPS)
        val modelMapObj = JSONObject(modelMapJson)
        val aiModels = getAllModels().associateBy { it.id }

        val small = aiModels[modelMapObj.getInt("s")] ?: error("Small model not found")
        val medium = aiModels[modelMapObj.getInt("m")] ?: error("Medium model not found")
        val large = aiModels[modelMapObj.getInt("l")] ?: error("Large model not found")
        val extraLarge = aiModels[modelMapObj.getInt("xl")] ?: error("Extra large model not found")

        return AiModelMap(small, medium, large, extraLarge)
    }

    fun getGenerationParameters(): GenerationParameters {
        val genParamsJsonStr = remoteConfig.getString(REMOTE_KEY_GENERATION_PARAMETERS)
        val genParams = JSONObject(genParamsJsonStr)

        return GenerationParameters(
            doSample = genParams.getBoolean("doSample"),
            temperature = genParams.getDouble("temperature").toFloat(),
            topK = genParams.getInt("topK"),
            topP = genParams.getDouble("topP").toFloat(),
            repetitionPenalty = genParams.getDouble("repetitionPenalty").toFloat(),
            maxNewTokens = genParams.getInt("maxNewTokens")
        )
    }

    fun getAnalyticsBaseUrl(): String? {
        try {
            val url = remoteConfig.getString(REMOTE_KEY_URL_ANALYTICS)
            if (url.isEmpty()) {
                return null
            }
            return url
        } catch (e: Exception) {
            return null
        }
    }

}