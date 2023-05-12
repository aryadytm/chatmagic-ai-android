package dev.bytebooster.chatmagicai.model

import androidx.annotation.DrawableRes

data class ChatMessage(
    val sender: String,
    val content: String,
    @DrawableRes val senderAvatar: Int,
    val isUser: Boolean
)
