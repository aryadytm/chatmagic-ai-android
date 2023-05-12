package dev.bytebooster.chatmagicai.model.net

data class Message(
    val conversation_id: String,
    val sender: String,
    val content: String,
    val turn_index: Int,
    val is_user: Boolean
)