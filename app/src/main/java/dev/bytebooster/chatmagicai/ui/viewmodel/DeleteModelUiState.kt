package dev.bytebooster.chatmagicai.ui.viewmodel

import dev.bytebooster.chatmagicai.model.AiModel

data class DeleteModelUiState(
    val downloadedModels: List<AiModel>,
    val selectedModelIndex: Int,
)
