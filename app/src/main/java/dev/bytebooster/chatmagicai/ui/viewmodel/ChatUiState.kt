package dev.bytebooster.chatmagicai.ui.viewmodel

import dev.bytebooster.chatmagicai.model.ChatMessage

data class ChatUiState(
    val messages: List<ChatMessage> = listOf(),
    val chatTextFieldValue: String = "",
    val isGenerating: Boolean = false,
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val showFeedbackButtons: Boolean = false,
)
