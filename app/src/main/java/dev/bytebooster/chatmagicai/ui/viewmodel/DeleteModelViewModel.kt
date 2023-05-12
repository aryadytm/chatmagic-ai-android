package dev.bytebooster.chatmagicai.ui.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.bytebooster.chatmagicai.R
import dev.bytebooster.chatmagicai.ai.AiModelDatasource
import dev.bytebooster.chatmagicai.ai.AiModelManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DeleteModelViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val modelDatasource: AiModelDatasource,
    private val modelManager: AiModelManager,
): ViewModel() {

    private val toastDeleteFailedUsedModel = Toast.makeText(context, context.getString(R.string.unable_to_delete_aca), Toast.LENGTH_SHORT)
    private val toastDeleteSuccess = Toast.makeText(context, context.getString(R.string.success_delete_ai_model_file), Toast.LENGTH_SHORT)

    private val _uiState = MutableStateFlow(DeleteModelUiState(
        downloadedModels = modelManager.getDownloadedModels().map { it.model },
        selectedModelIndex = 0
    ))

    val uiState = _uiState.asStateFlow()

    fun deleteModel() {
        val model = uiState.value.downloadedModels[uiState.value.selectedModelIndex]
        val usedModel = modelDatasource.getUsedModel()

        if (usedModel != null && usedModel == model) {
            toastDeleteFailedUsedModel.show()
            return
        }

        modelManager.delete(model)

        toastDeleteSuccess.show()
        _uiState.update { uis ->
            uis.copy(downloadedModels = modelManager.getDownloadedModels().map { it.model })
        }
    }

    fun onSelectedModelChanged(idx: Int) {
        _uiState.update { it.copy(selectedModelIndex = idx) }
    }

}