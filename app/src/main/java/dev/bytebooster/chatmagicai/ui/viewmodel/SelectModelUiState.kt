package dev.bytebooster.chatmagicai.ui.viewmodel

import dev.bytebooster.chatmagicai.model.AiModel

data class SelectModelUiState(
    val startingModels: List<AiModel> = listOf(),
    val selectedModelCard: AiModel,
    val downloadProgress: Float = 0f,
)