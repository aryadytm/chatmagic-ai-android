package dev.bytebooster.chatmagicai.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.bytebooster.chatmagicai.R
import dev.bytebooster.chatmagicai.addNewLinesBetweenLists
import dev.bytebooster.chatmagicai.ai.AiModelDatasource
import dev.bytebooster.chatmagicai.ai.AiModelManager
import dev.bytebooster.chatmagicai.ai.textgen.TextGenerator
import dev.bytebooster.chatmagicai.ai.textgen.stripNonASCII
import dev.bytebooster.chatmagicai.data.RemoteConfigDatasource
import dev.bytebooster.chatmagicai.logDebug
import dev.bytebooster.chatmagicai.model.AiModel
import dev.bytebooster.chatmagicai.model.ChatMessage
import dev.bytebooster.chatmagicai.model.GenerationParameters
import dev.bytebooster.chatmagicai.model.InferenceAiModel
import dev.bytebooster.chatmagicai.net.AnalyticsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val aiModelManager: AiModelManager,
    private val aiModelSource: AiModelDatasource,
    private val remoteConfigDatasource: RemoteConfigDatasource,
    private val analyticsHelper: AnalyticsHelper,
    @ApplicationContext context: Context,
) : ViewModel() {

    // Static dependencies
    private val analytics = Firebase.analytics
    private val languageIdentifier = LanguageIdentification.getClient()
    private val translateModelManager = RemoteModelManager.getInstance()

    // Chat Message Templates
    private val aiMessageTemplate = ChatMessage(
        sender = "ChatMagic AI",
        content = "",
        senderAvatar = R.drawable.ic_chatmagicai,
        isUser = false
    )
    private val userMessageTemplate = ChatMessage(
        sender = "You",
        content = "",
        senderAvatar = R.drawable.ic_user,
        isUser = true
    )

    // Strings
    private val loadingText = "Loading..."
    private val welcomeText = context.getString(R.string.hello_im_chatmagic)
    private val loadModelErrorText = context.getString(R.string.there_is_an_error)
    private val botWritingText = context.getString(R.string.chatmagic_ai_writing)

    // UI state
    private val _uiState = MutableStateFlow(ChatUiState(messages = listOf()))
    val uiState = _uiState.asStateFlow()

    // Dynamic states
    private var downloadedTranslationModels: List<String> = listOf()
    private var generationParameters = GenerationParameters(
        // Default generation parameters. This will dynamically change based on remote config
        doSample = true,
        temperature = 0.5f,
        topK = 4,
        topP = 0.95f,
        repetitionPenalty = 1.04f,
        maxNewTokens = 320,
    )

    // Dynamic states (lateinit)
    private lateinit var textgen: TextGenerator
    private lateinit var currentInferenceModel: InferenceAiModel

    init {
        initialAiMessage(loadingText)
        viewModelScope.launch {
            uiState.collect {
                val sender = if (it.isGenerating) botWritingText else "ChatMagic AI"
                updateLastMessage { msg ->
                    if (!msg.isUser) msg.copy(sender = sender) else msg
                }
            }
        }
    }

    fun updateUiState(uiState: ChatUiState) {
        _uiState.update { uiState }
    }

    fun loadPage() {
        logDebug("Loading page...")

        generationParameters = remoteConfigDatasource.getGenerationParameters()

        translateModelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { models ->
                downloadedTranslationModels = models.map { it.language }
                logDebug("Downloaded translation models: $downloadedTranslationModels")
            }
            .addOnFailureListener {
                logDebug("Error getting downloaded translation models: $it")
            }

        viewModelScope.launch(Dispatchers.IO) {
            val usedModel = aiModelSource.getUsedModel()
            logDebug("Used model: $usedModel")

            if (usedModel == null || !aiModelManager.isExists(usedModel)) {
                if (usedModel == null)
                    onLoadModelFailed("Used model is null!")
                else if (!aiModelManager.isExists(usedModel))
                    onLoadModelFailed("Used model file is not exists!")
                return@launch
            }

            val needReloadModel = if (this@ChatViewModel::currentInferenceModel.isInitialized) {
                usedModel.id != currentInferenceModel.model.id // Check if the used model is different
            } else {
                true
            }

            if (this@ChatViewModel::textgen.isInitialized && !needReloadModel) {
                // No need to reload the model. The text generator is ready.
                return@launch
            }

            logDebug("Loading model...")
            loadModel(usedModel)
        }
    }

    fun clickNewChat() {
        // Clear chats
        _uiState.update { it.copy(isGenerating = false) }
        initialAiMessage(welcomeText)
    }

    fun clickFeedback(isPositive: Boolean) {
        // Update ui state to hide feedback buttons
        _uiState.update { it.copy(showFeedbackButtons = false) }

        val messages = uiState.value.messages.filterIndexed { index, _ -> index != 0 }

        analyticsHelper.sendFeedback(
            messages,
            currentInferenceModel.model,
            isPositive,
            onError = { logDebug("Error sending feedback: $it") },
            onSuccess = { logDebug("Feedback sent successfully") }
        )
    }

    fun clickSendMessage() {
        // Sanitize string from user input
        _uiState.update {
            it.copy(chatTextFieldValue = it.chatTextFieldValue.stripNonASCII().trim())
        }

        if (uiState.value.isGenerating) {
            // UI state is currently generating. Then the user clicked the stop button. We should stop generating
            _uiState.update {
                it.copy(isGenerating = false)
            }
            return
        }

        if (uiState.value.chatTextFieldValue.isEmpty()) {
            // User input is empty. Do nothing
            return
        }

        // Format messages by preparing an empty AI message that will be stream updated later
        val userMessage = userMessageTemplate.copy(content = uiState.value.chatTextFieldValue)
        val aiMessage = aiMessageTemplate.copy(content = "")

        // Update UI state and set generating to True
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage + aiMessage,
                chatTextFieldValue = "",
                isGenerating = true
            )
        }

        viewModelScope.launch(Dispatchers.Default) {
            startGenerate()
        }
    }

    fun clickRegenerate() {
        // Regenerate the last AI message

        // If the last message is not AI message, do nothing
        if (uiState.value.messages.last().isUser) return
        // Make the last AI message empty then set generating to True
        _uiState.update {
            it.copy(
                messages = it.messages.dropLast(1) + aiMessageTemplate.copy(content = ""),
                isGenerating = true
            )
        }
        // Start text generation
        viewModelScope.launch(Dispatchers.Default) {
            startGenerate()
        }
    }

    fun setGenerationParameters(generationParameters: GenerationParameters) {
        this.generationParameters = generationParameters
    }

    private suspend fun startGenerate() {
        // Start text generation based on UI messages
        val promptPair = preparePrompt()
        val prompt = promptPair.first
        val userLanguage = promptPair.second
        val translator = getTranslator(TranslateLanguage.ENGLISH, userLanguage)

        logDebug("\n------------------------------\n${generationParameters}\n------------------------------")

        var lastTokenTime = System.currentTimeMillis()
        var cumTokens = ""
        var translatesLeft = 0

        textgen.generate(
            promptText = prompt.trim(),
            doSample = generationParameters.doSample,
            temperature = generationParameters.temperature,
            topK = generationParameters.topK,
            topP = generationParameters.topP,
            repetitionPenalty = generationParameters.repetitionPenalty,
            maxNewTokens = generationParameters.maxNewTokens,
        )
        { newToken ->
            // Log.v("CMA_BENCHMARK", "Token Latency: ${System.currentTimeMillis() - lastTokenTime}")

            lastTokenTime = System.currentTimeMillis()
            cumTokens += newToken

            if (userLanguage != TranslateLanguage.ENGLISH) {
                // Translate the generated text to user language
                onTokenWithTranslate(newToken, cumTokens, translator, false)
            } else {
                // Vanilla english text generation
                onToken(newToken, cumTokens)
            }

            if ("<|" in cumTokens || "endoftext" in cumTokens) {
                logDebug("Generation stopped by special token ($newToken)")

                if (userLanguage == TranslateLanguage.ENGLISH)
                    _uiState.update { it.copy(isGenerating = false) }
                else return@generate false
            }

            return@generate uiState.value.isGenerating
        }

        // Wait for all translations to complete
        if (userLanguage != TranslateLanguage.ENGLISH) {
            onTokenWithTranslate("", cumTokens, translator, true)
            delay(1000)
            _uiState.update { it.copy(isGenerating = false) }
        }

        onFinishGenerating(prompt, cumTokens)
    }


    private fun onTokenWithTranslate(newToken: String, cumTokens: String, translator: Translator, translateAll: Boolean) {
        if (!translateAll && !("." in newToken || "!" in newToken || "?" in newToken))
            return

        translator.translate(cumTokens)
            .addOnSuccessListener { translatedText ->
                onToken(newToken, translatedText.addNewLinesBetweenLists())
            }
            .addOnFailureListener { exception ->
                logDebug("Translate generated text failed: ${exception.message}")
            }
    }

    private fun onFinishGenerating(prompt: String, cumTokens: String) {
        _uiState.update { it.copy(isGenerating = false, showFeedbackButtons = true) }

        analytics.logEvent("chat") {
            param("model_id", currentInferenceModel.model.id.toString())
            param("prompt", prompt.substring(prompt.length - prompt.length.coerceAtMost(500), prompt.length))
            param("response", cumTokens)
        }

        logDebug("\n------------------------------\n${prompt + cumTokens}\n------------------------------")
    }

    private suspend fun preparePrompt(): Pair<String, String> {
        // Don't include the AI welcome message. Also don't include the last empty AI message.
        val messagesOfPrompt = uiState.value.messages.subList(1, uiState.value.messages.size - 1)
        val userMessage = messagesOfPrompt.joinToString { it.content }

        // Get the user language
        val userLanguage = languageIdentifier.identifyLanguage(userMessage).await()
        val userLanguageObj = TranslateLanguage.fromLanguageTag(userLanguage) ?: TranslateLanguage.ENGLISH
        val shouldTranslate = (
            userLanguageObj != TranslateLanguage.ENGLISH &&
            userLanguageObj in downloadedTranslationModels &&
            userMessage.split(" ").size >= 4
        )

        var promptMessages = messagesOfPrompt.toList()

        if (shouldTranslate) {
            // If the user language is other than English, translate it to English. But in undetected, stay in English.
            promptMessages = translateMessages(promptMessages, userLanguageObj, TranslateLanguage.ENGLISH)
        }

        val prompt = currentInferenceModel.formatter.preparePrompt(
            messages = promptMessages,
            tokenizer = textgen.tokenizer,
        )

        return Pair(prompt, userLanguageObj)
    }

    private suspend fun translateMessages(messages: List<ChatMessage>, from: String, to: String): List<ChatMessage> {
        val translator = getTranslator(from, to)
        val translatedMessages = messages.map {
            it.copy(
                content = translator.translate(it.content).await()
            )
        }
        return translatedMessages
    }

    private fun onToken(token: String, cumToken: String) {
        if (!uiState.value.isGenerating) {
            return
        }

        var response = cumToken.split("<|")[0].stripNonASCII().trim()
        response = response.removeSuffix("<")

        updateLastMessage {
            it.copy(content = response)
        }
    }

    private fun updateLastMessage(getUpdatedLastMessage: (ChatMessage) -> ChatMessage) {
        val messages =  uiState.value.messages
        val lastMessage = messages[messages.lastIndex]
        val updatedLastMessage = getUpdatedLastMessage(lastMessage)
        val oldMessages = messages.slice(0 until messages.size - 1)
        _uiState.update {
            it.copy(messages = oldMessages + updatedLastMessage)
        }
    }

    private fun initialAiMessage(text: String) {

        val message = ChatMessage(
            sender = "ChatMagic AI",
            content = text,
            senderAvatar = R.drawable.ic_chatmagicai,
            isUser = false,
        )

        _uiState.update {
            it.copy(messages = listOf(message))
        }
    }

    private fun getTranslator(from: String, to: String): Translator {
        logDebug("Get translator: from $from to $to")
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(from)
            .setTargetLanguage(to)
            .build()
        return Translation.getClient(options)
    }

    private suspend fun loadModel(model: AiModel) {

        initialAiMessage(loadingText)
        _uiState.update { it.copy(isLoading = true) }

        try {
            if (this@ChatViewModel::textgen.isInitialized) {
                aiModelManager.unload(textgen)
            }

            textgen = aiModelManager.loadModel(model)
            currentInferenceModel = aiModelSource.getInferenceModel(model)

            initialAiMessage(welcomeText)
            _uiState.update { it.copy(isLoading = false) }
        }
        catch (e: Exception) {
            FirebaseCrashlytics.getInstance().log("Load model failed: ${e.message}")
            e.printStackTrace()
            onLoadModelFailed(e.message)
            return
        }
    }

    private fun onLoadModelFailed(message: String?) {
        // Delete the model and kick the user from chat screen.
        initialAiMessage(loadModelErrorText)

        val usedModel = aiModelSource.getUsedModel()

        aiModelSource.unuseModel()

        usedModel?.let {
            logDebug("Unuse and delete model: $it")
            aiModelManager.delete(it)
        }

        viewModelScope.launch {
            delay(10000)
            throw Exception("Load model failed due to `$message`. Exit the app.")
        }
    }


}