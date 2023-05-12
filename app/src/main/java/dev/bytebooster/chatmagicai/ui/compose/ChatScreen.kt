package dev.bytebooster.chatmagicai.ui.compose

import android.content.res.Configuration
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.*
import dev.bytebooster.chatmagicai.R
import dev.bytebooster.chatmagicai.data.FakeDatasource
import dev.bytebooster.chatmagicai.model.ChatMessage
import dev.bytebooster.chatmagicai.simpleVerticalScrollbar
import dev.bytebooster.chatmagicai.ui.theme.*
import dev.bytebooster.chatmagicai.ui.viewmodel.ChatInteraction
import dev.bytebooster.chatmagicai.ui.viewmodel.ChatUiState
import dev.bytebooster.chatmagicai.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



@Preview()
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewChatAct() {
    val chatListState = rememberLazyListState()

    val inter = ChatInteraction(
        uiState = ChatUiState(messages = FakeDatasource().loadChats(), showFeedbackButtons = true),
        chatListState = chatListState,
        onMenuIconClicked = {},
        onPlusIconClicked = {},
        onChatTextFieldChanged = {},
        onSendButtonClicked = {},
        onScrollToBottom = {},
        onRegenerateButtonClicked = {},
        onFeedbackPositiveButtonClicked = {},
        onFeedbackNegativeButtonClicked = {},
    )

    ChatMagicAITheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ChatLayout(inter = inter)
        }
    }
}

@Composable
fun ChatScreen(
    onMenuIconClicked: () -> Unit,
    onEmptyModel: () -> Unit,
    chatViewModel: ChatViewModel,
) {
    val chatUiState by chatViewModel.uiState.collectAsState()
    val chatListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val inter = ChatInteraction(
        uiState = chatUiState,
        chatListState = chatListState,
        onMenuIconClicked = onMenuIconClicked,
        onPlusIconClicked = {
            chatViewModel.clickNewChat()
        },
        onChatTextFieldChanged = {
            chatViewModel.updateUiState(chatUiState.copy(chatTextFieldValue = it))
        },
        onSendButtonClicked = {
            chatViewModel.clickSendMessage()
        },
        onRegenerateButtonClicked = {
            chatViewModel.clickRegenerate()
        },
        onFeedbackPositiveButtonClicked = {
            chatViewModel.clickFeedback(isPositive = true)
        },
        onFeedbackNegativeButtonClicked = {
            chatViewModel.clickFeedback(isPositive = false)
        },
        onScrollToBottom = {
            coroutineScope.launch {
                val lastVisibleIdx = chatListState.layoutInfo.visibleItemsInfo[
                        chatListState.layoutInfo.visibleItemsInfo.size - 1
                ].index

                val shouldScrollToBottom = lastVisibleIdx >= chatUiState.messages.lastIndex

                if (shouldScrollToBottom) {
                    delay(100)
                    chatListState.scrollBy(3000f)
//                    logDebug("Scrolled to bottom")
                }
            }

        },
    )

    LaunchedEffect(Unit){
        chatViewModel.loadPage()
        if (chatUiState.isError) {
            onEmptyModel()
        }
    }

    SelectionContainer {
        ChatLayout(inter = inter)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatLayout(
    inter: ChatInteraction
) {

//    Column() {
//        ChatScreenTopBar(inter = inter)
//        ChatList(
//            inter = inter,
//            paddings = PaddingValues(),
//            modifier = Modifier.padding().fillMaxHeight().weight(1f)
//        )
//        ChatScreenBottomBar(inter = inter)
//    }

    Scaffold(
        topBar = { ChatScreenTopBar(inter = inter) },
        bottomBar = { ChatScreenBottomBar(inter = inter) }
    ) { scaffoldPadding ->
        ChatList(
            inter = inter,
            paddings = scaffoldPadding,
            modifier = Modifier.padding(scaffoldPadding),
        )
    }
}


@Composable
fun ChatScreenTopBar(
    inter: ChatInteraction,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            IconButton(onClick = inter.onMenuIconClicked, modifier = Modifier) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_menu_24),
                    contentDescription = null,
                    modifier = Modifier,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(R.string.chat_with_chatmagic_ai),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            if (!inter.uiState.isLoading) {
                IconButton(onClick = inter.onPlusIconClicked, modifier = Modifier) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_add_24),
                        contentDescription = null,
                        modifier = Modifier,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
        Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant)
    }

}


@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun ChatScreenBottomBar(
    inter: ChatInteraction,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val btnIcon = if (inter.uiState.isGenerating) painterResource(id = R.drawable.baseline_stop_24)
                    else painterResource(id = R.drawable.baseline_send_24)
    val btnIconTint = Color.White
    val btnBgColor = if (inter.uiState.isGenerating) Color.Gray else MaterialTheme.colorScheme.primary

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val bringIntoViewRequester = BringIntoViewRequester()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .bringIntoViewRequester(bringIntoViewRequester)
            .padding(top = 2.dp, bottom = 4.dp)
    ) {
        TextField(
            value = inter.uiState.chatTextFieldValue,
            onValueChange = { inter.onChatTextFieldChanged(it) },
            singleLine = false,
            maxLines = 10,
            textStyle = MaterialTheme.typography.bodyLarge,
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.background,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            placeholder = {
                Text(text = stringResource(R.string.message))
            },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
                .border(
                    BorderStroke((0.2).dp, SolidColor(MaterialTheme.colorScheme.surfaceVariant)),
                    RoundedCornerShape(24.dp)
                )
                .onFocusEvent { it ->
                    if (it.isFocused) {
                        coroutineScope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                        inter.onScrollToBottom()
                    }
                }
        )
        Button(
            onClick = {
                focusManager.clearFocus()
                keyboardController?.hide()
                inter.onSendButtonClicked()
            },
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .size(50.dp)
                .padding(end = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = btnBgColor),
            enabled = !inter.uiState.isLoading,
        ) {
            Icon(
                painter = btnIcon,
                contentDescription = null,
                tint = btnIconTint
            )
        }
    }

}


@Composable
fun ChatList(
    inter: ChatInteraction,
    paddings: PaddingValues,
    modifier: Modifier = Modifier,
) {

    LaunchedEffect(inter.uiState.messages.size) {
        inter.chatListState.scrollToItem(inter.uiState.messages.size)
    }

    LaunchedEffect(inter.uiState.messages[inter.uiState.messages.size - 1].content.length) {
        inter.chatListState.scrollBy(1000f)
    }

    LazyColumn(
        state = inter.chatListState,
        userScrollEnabled = !inter.uiState.isGenerating,
        modifier = modifier.simpleVerticalScrollbar(inter.chatListState, width = 4.dp)
    ) {
        items(inter.uiState.messages) {
            ChatItem(
                chat = it,
                // To show buttons, we need to check if the current item is the last item in the list and we are not generating.
                showButtons = (it == inter.uiState.messages.takeLast(1)[0] && inter.uiState.messages.size > 1) && !inter.uiState.isGenerating,
                inter = inter
            )
            Divider(thickness = (0.5).dp, color = MaterialTheme.colorScheme.surfaceVariant)
        }

    }
}


@Composable
fun ChatItem(chat: ChatMessage, inter: ChatInteraction, showButtons: Boolean) {
    val imgSenderAvatar = painterResource(id = chat.senderAvatar)
    val imgPositiveFeedbackBtn = painterResource(id = R.drawable.baseline_thumb_up_off_alt_24)
    val imgNegativeFeedbackBtn = painterResource(id = R.drawable.baseline_thumb_down_off_alt_24)
    val imgRegenerateBtn = painterResource(id = R.drawable.baseline_repeat_24)

    var avatarModifier = Modifier
        .size(24.dp)
        .clip(CircleShape)

    if (!chat.isUser) {
        avatarModifier = avatarModifier.graphicsLayer(
            scaleX = 1.35f,
            scaleY = 1.35f,
            rotationZ = 1f
        )
    }

    Column(
        modifier = Modifier
            .background(if (chat.isUser) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .size(48.dp)
                .padding(start = 16.dp, end = 16.dp)
        ) {
            Image(
                painter = imgSenderAvatar,
                contentDescription = null,
                modifier = avatarModifier
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = chat.sender,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (!chat.isUser) FontWeight.SemiBold else FontWeight.Medium,
                color = if (chat.isUser) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
//                    .alpha(if (chat.isUser) 0.6f else 1f)
            )

            if (showButtons) {

                if (inter.uiState.showFeedbackButtons) {
                    // Show feedback buttons
                    IconButton(
                        onClick = inter.onFeedbackPositiveButtonClicked,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imgPositiveFeedbackBtn,
                            null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .size(20.dp)
                                .alpha(0.3f)
                        )
                    }
                    IconButton(
                        onClick = inter.onFeedbackNegativeButtonClicked,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imgNegativeFeedbackBtn,
                            null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .size(20.dp)
                                .alpha(0.3f)
                        )
                    }
                }

                IconButton(
                    onClick = inter.onRegenerateButtonClicked,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imgRegenerateBtn,
                        null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(20.dp)
                            .alpha(0.3f)
                    )
                }
            }
        }
        Box(modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            .fillMaxWidth()
        ) {
//            Text(
//                text = chat.content,
//                style = MaterialTheme.typography.bodyMedium,
//                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
//            )
//            MarkdownText(
//                markdown = chat.content,
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onBackground,
//                fontSize = 14.sp,
//                fontResource = R.font.inter_regular,
//            )
//            MDView(text = chat.content)
//            Markdown(
//                content = chat.content,
//                colors = MarkdownDefaults.markdownColors(textColor = MaterialTheme.colorScheme.onBackground),
//                typography = MarkdownDefaults.markdownTypography(
//                    h1 = MaterialTheme.typography.bodyMedium,
//                    h2 = MaterialTheme.typography.bodyMedium,
//                    h3 = MaterialTheme.typography.bodyMedium,
//                    h4 = MaterialTheme.typography.bodyMedium,
//                    h5 = MaterialTheme.typography.bodyMedium,
//                    h6 = MaterialTheme.typography.bodyMedium,
//                    body1 = MaterialTheme.typography.bodyMedium,
//                    body2 = MaterialTheme.typography.bodyMedium,
//                )
//            )
//            Material3RichText(
//                modifier = Modifier,
//                style = RichTextStyle(
//                    headingStyle = { level: Int, textStyle: TextStyle ->
//                        TextStyle(fontSize = 14.sp)
//                    },
//                    stringStyle = RichTextStringStyle(
//                        codeStyle = SpanStyle(
//                            color = MaterialTheme.colorScheme.onBackground,
//                            fontSize = 14.sp,
//                        ),
//                    ),
//                )
//            ) {
//                Markdown(content = "${chat.content}")
//            }

            RichTextThemeIntegration(
                textStyle = { MaterialTheme.typography.bodyMedium },
                contentColor = { MaterialTheme.colorScheme.onBackground },
            ) {
                RichText(style = RichTextStyle(
                    paragraphSpacing = 8.sp,
                    headingStyle = { level: Int, textStyle: TextStyle ->
                        TextStyle(fontSize = 14.sp)
                    },
                    listStyle = ListStyle(
                        markerIndent = 0.sp,
                        contentsIndent = 4.sp,
                        itemSpacing = 0.sp,
                    ),
                    codeBlockStyle = CodeBlockStyle(
                        textStyle = TextStyle(fontSize = 14.sp),
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxWidth()
                            .border(0.5.dp, color = Color.Gray)
                    ),
                )) {
                    Markdown(content = chat.content)
                }
            }
        }
    }
}
