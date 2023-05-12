package dev.bytebooster.chatmagicai.model.net


data class Feedback(
    val conversation_id: String,
    val model_id: String,
    val is_positive: Boolean
)
