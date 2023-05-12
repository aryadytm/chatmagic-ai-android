package dev.bytebooster.chatmagicai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bytebooster.chatmagicai.ai.textgen.TextGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bytebooster.chatmagicai.R
import dev.bytebooster.chatmagicai.ai.AiModelDatasource
import dev.bytebooster.chatmagicai.ai.AiModelManager
import dev.bytebooster.chatmagicai.ai.textgen.stripNonASCII
import dev.bytebooster.chatmagicai.logDebug
import dev.bytebooster.chatmagicai.model.AiModel
import dev.bytebooster.chatmagicai.model.ChatMessage
import dev.bytebooster.chatmagicai.model.InferenceAiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel_NoTrans @Inject constructor(
    private val modelManager: AiModelManager,
    private val modelSource: AiModelDatasource,
) : ViewModel() {

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

    private val loadingText = "Loading..."
    private val welcomeText = (
        "Hi! I'm ChatMagic AI. I'm here to assist you.\n\n" +
        "I can do the following:\n" +
        "1. Answer questions and give explanations\n" +
        "2. Assist in writing a text based content\n" +
        "3. Follow simple instructions\n\n" +
        "However, I still have limitations. I may write incorrect information or produce harmful instructions. " +
        "Please use me with caution."
    )
    private val loadModelErrorText = "There is an error on loading the AI model (the AI model may be corrupted). " +
            "Please restart the app to download it again."
    private val nullModelErrorText = "There is an error on getting responses from the AI model. " +
            "Please try restarting the app."

    private val _uiState = MutableStateFlow(ChatUiState(messages = listOf()))
    val uiState = _uiState.asStateFlow()

    private lateinit var textgen: TextGenerator
    private lateinit var currentInferenceModel: InferenceAiModel

    init {
        initialAiMessage(loadingText)
    }

    fun updateUiState(uiState: ChatUiState) {
        _uiState.update { uiState }
    }

    fun loadPage() {
        val model = modelSource.getUsedModel()

        if (model == null || !userCanChat()) {
            logDebug("Load model failed: Model is null or user can't chat.")
            onLoadModelFailed()
            return
        }

        val needReloadModel = if (this::currentInferenceModel.isInitialized) {
             model.id != currentInferenceModel.model.id
        } else {
            true
        }

        if (this::textgen.isInitialized && !needReloadModel) {
            // No need to reload the model. The text gen is ready.
            return
        }

        logDebug("Loading model...")
        loadModel(model)
    }

    private fun userCanChat(): Boolean {
        return (modelSource.getUsedModel() != null) && (modelManager.isExists(modelSource.getUsedModel()!!))
    }

    fun clickPlusBtn() {
        // Clear chats
        _uiState.update { it.copy(isGenerating = false) }
        initialAiMessage(welcomeText)
    }

    fun clickMessageBtn() {

        _uiState.update {
            // Sanitize string
            it.copy(chatTextFieldValue = it.chatTextFieldValue.stripNonASCII().trim())
        }

        if (uiState.value.isGenerating) {
            // User clicked the stop button. We should stop generating
            _uiState.update {
                it.copy(isGenerating = false)
            }
            return
        }

        if (uiState.value.chatTextFieldValue.isEmpty()) {
            return
        }

        val userMessage = userMessageTemplate.copy(content = uiState.value.chatTextFieldValue)
        val aiMessage = aiMessageTemplate.copy(content = "")

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage + aiMessage,
                chatTextFieldValue = "",
                isGenerating = true
            )
        }

        viewModelScope.launch(Dispatchers.Default) {

            if (!this@ChatViewModel_NoTrans::textgen.isInitialized) {
                initialAiMessage(nullModelErrorText)
                return@launch
            }

            val prompt = currentInferenceModel.formatter.preparePrompt(
                // Don't include the AI welcome message. Also don't include the last empty AI message.
                messages = uiState.value.messages.subList(1, uiState.value.messages.size - 1),
                tokenizer = textgen.tokenizer,
            )

            logDebug("\n------------------------------\n$prompt\n------------------------------")

            var lastTokenTime = System.currentTimeMillis()

            textgen.generate(
                promptText = prompt,
                doSample = true,
                temperature = 0.5f,
                topK = 4,
                topP = 0.95f,
                repetitionPenalty = 1.03f,
                maxNewTokens = 256,
                onToken = { newToken ->
                    // Log.v("CMA_BENCHMARK", "Token Latency: ${System.currentTimeMillis() - lastTokenTime}")
                    lastTokenTime = System.currentTimeMillis()
                    onToken(newToken)
                    return@generate uiState.value.isGenerating
                }
            )

            _uiState.update { it.copy(isGenerating = false) }
        }
    }

    private fun loadModel(model: AiModel) {
        viewModelScope.launch(Dispatchers.IO) {

            initialAiMessage(loadingText)
            _uiState.update { it.copy(isLoading = true) }

            try {
                if (this@ChatViewModel_NoTrans::textgen.isInitialized) {
                    modelManager.unload(textgen)
                }

                textgen = modelManager.loadModel(model)
                currentInferenceModel = modelSource.getInferenceModel(model)

                initialAiMessage(welcomeText)
                _uiState.update { it.copy(isLoading = false) }
            }
            catch (e: Exception) {
                e.printStackTrace()
                onLoadModelFailed()
                return@launch
            }
        }
    }

    private fun onLoadModelFailed() {
        // Delete the model and kick the user from chat screen.
        initialAiMessage(loadModelErrorText)
        modelSource.getUsedModel()?.let {
            logDebug("Unuse and delete model: $it")
            modelSource.unuseModel()
            modelManager.delete(it)
            return@let
        }
    }

    private fun onToken(token: String) {

        if ("endoftext" in token) return

        val messages = uiState.value.messages
        val chatMagicAiMessage = uiState.value.messages[messages.size - 1]
        val newChatMagicAiMessage = chatMagicAiMessage.copy(
            content = (chatMagicAiMessage.content + token.stripNonASCII()).trimStart()
        )
        val newMessages = messages.slice(0 until messages.size - 1)

        _uiState.update {
            it.copy(messages = newMessages + newChatMagicAiMessage)
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

}