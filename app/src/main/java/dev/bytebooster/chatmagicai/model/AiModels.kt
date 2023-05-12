package dev.bytebooster.chatmagicai.model

import dev.bytebooster.chatmagicai.ai.ChatFormatter
import java.io.File


const val MODEL_TYPE_TEST = "test"
const val MODEL_TYPE_SMALL = "s"
const val MODEL_TYPE_MEDIUM = "m"
const val MODEL_TYPE_LARGE = "l"


data class AiModel(
    val id: Int,
    val name: String,
    val type: String,
    val description: String,
    val downloadUrl: String,
    val size: Long,
    val formatter: String,
)


data class LocalAiModel(
    val model: AiModel,
    val file: File
)


data class InferenceAiModel(
    val model: AiModel,
    val formatter: ChatFormatter
)


data class AiModelMap(
    val small: AiModel,
    val medium: AiModel,
    val large: AiModel,
    val extraLarge: AiModel
)