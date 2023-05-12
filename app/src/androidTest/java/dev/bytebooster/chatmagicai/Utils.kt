package dev.bytebooster.chatmagicai

import dev.bytebooster.chatmagicai.model.ChatMessage

fun getChatMessage(isUser: Boolean, content: String): ChatMessage {
    return ChatMessage(isUser = isUser, content = content, sender = if (!isUser) "ChatMagicAI" else "You", senderAvatar = 0)
}