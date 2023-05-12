package dev.bytebooster.chatmagicai.ui.viewmodel

import androidx.compose.foundation.lazy.LazyListState

class ChatInteraction(
    val uiState: ChatUiState,
    val chatListState: LazyListState,
    val onMenuIconClicked: () -> Unit,
    val onPlusIconClicked: () -> Unit,
    val onChatTextFieldChanged: (String) -> Unit,
    val onSendButtonClicked: () -> Unit,
    val onScrollToBottom: () -> Unit,
    val onRegenerateButtonClicked: () -> Unit,
    val onFeedbackPositiveButtonClicked: () -> Unit,
    val onFeedbackNegativeButtonClicked: () -> Unit,
) {

}