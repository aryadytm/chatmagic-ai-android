package dev.bytebooster.chatmagicai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bytebooster.chatmagicai.data.RemoteConfigDatasource
import dev.bytebooster.chatmagicai.data.WelcomeDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val remoteConfigDatasource: RemoteConfigDatasource,
    private val welcomeDataSource: WelcomeDataSource,
): ViewModel(){

    private val _uiState = MutableStateFlow(WelcomeUiState(
        isUserMessageVisible = false,
        isAiMessageVisible = false,
        isTextDescVisible = false,
        userMessage = welcomeDataSource.getUserMessage().copy(content = ""),
        aiMessage = welcomeDataSource.getAiMessage().copy(content = "")
    ))
    val uiState = _uiState.asStateFlow()

    val privacyPolicyUrl = remoteConfigDatasource.privacyPolicyUrl
    val termsOfServiceUrl = remoteConfigDatasource.termsOfServiceUrl
    val legalUrl = remoteConfigDatasource.legalUrl

    fun loadPage() {
        if (uiState.value.isUserMessageVisible) {
            // Don't reanimate
            return
        }
        viewModelScope.launch {
            animateWelcome()
        }
    }

    private suspend fun animateWelcome() {
        resetUiState()

        delay(100)
        _uiState.update { it.copy(isUserMessageVisible = true) }  // TODO: Animate Fade?

        for (character in welcomeDataSource.getUserMessage().content) {

            delay(50)
            _uiState.update { it ->
                it.copy(userMessage = uiState.value.userMessage.copy(
                    content = uiState.value.userMessage.content + character
                ))
            }
        }

        delay(1000)
        _uiState.update { it.copy(isAiMessageVisible = true) }

        for (character in welcomeDataSource.getAiMessage().content) {

            delay(50)
            _uiState.update { it ->
                it.copy(aiMessage = uiState.value.aiMessage.copy(
                    content = uiState.value.aiMessage.content + character
                ))
            }
        }

        delay(1000)
        _uiState.update { it.copy(isTextDescVisible = true) }

    }

    private fun resetUiState() {
        _uiState.update {
            WelcomeUiState(
                isUserMessageVisible = false,
                isAiMessageVisible = false,
                isTextDescVisible = false,
                userMessage = welcomeDataSource.getUserMessage().copy(content = ""),
                aiMessage = welcomeDataSource.getAiMessage().copy(content = "")
            )
        }
    }


}