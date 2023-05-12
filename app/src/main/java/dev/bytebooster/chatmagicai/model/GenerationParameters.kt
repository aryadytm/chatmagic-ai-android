package dev.bytebooster.chatmagicai.model

data class GenerationParameters(
    val doSample: Boolean,
    val temperature: Float,
    val topK: Int,
    val topP: Float,
    val repetitionPenalty: Float,
    val maxNewTokens: Int,
)
