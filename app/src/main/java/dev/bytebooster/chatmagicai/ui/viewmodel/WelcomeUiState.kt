package dev.bytebooster.chatmagicai.ui.viewmodel

import dev.bytebooster.chatmagicai.model.ChatMessage

data class WelcomeUiState(
    val isUserMessageVisible: Boolean,
    val isAiMessageVisible: Boolean,
    val isTextDescVisible: Boolean,
    val userMessage: ChatMessage,
    val aiMessage: ChatMessage,
)
