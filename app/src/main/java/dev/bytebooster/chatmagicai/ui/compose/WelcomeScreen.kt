package dev.bytebooster.chatmagicai.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.bytebooster.chatmagicai.R
import dev.bytebooster.chatmagicai.data.FakeDatasource
import dev.bytebooster.chatmagicai.model.ChatMessage
import dev.bytebooster.chatmagicai.openInBrowser
import dev.bytebooster.chatmagicai.ui.theme.ChatMagicAITheme
import dev.bytebooster.chatmagicai.ui.viewmodel.WelcomeViewModel


@Preview(showBackground = true)
@Composable
fun PreviewWelcomeScreen() {
    WelcomeAct()
}


@Composable
fun WelcomeAct() {
    ChatMagicAITheme() {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            WelcomeScreen({})
        }
    }
}


@Composable
fun WelcomeScreen(
    onContinueClicked: () -> Unit,
    welcomeViewModel: WelcomeViewModel = hiltViewModel()
) {
    val uiState by welcomeViewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current

    val chats = FakeDatasource().loadChats()
    val legalURL = welcomeViewModel.legalUrl

    LaunchedEffect(Unit) {
        welcomeViewModel.loadPage()
    }


    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.padding(top = 24.dp))
            Text(
                text = stringResource(R.string.welcome_to),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
            Text(
                text = stringResource(id = R.string.my_app_name),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.padding(top = 24.dp))

            AnimatedVisibility(visible = uiState.isUserMessageVisible) {
                MessageBox(chat = uiState.userMessage)
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            AnimatedVisibility(visible = uiState.isAiMessageVisible) {
                MessageBox(chat = uiState.aiMessage)
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            AnimatedVisibility(visible = uiState.isTextDescVisible) {
                Text(
                    text = stringResource(R.string.your_personal_ai_assistant_is_rthya),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }

        Column(modifier = Modifier
            .align(Alignment.BottomCenter)
            .background(MaterialTheme.colorScheme.background)
        ) {

            Button(
                onClick = onContinueClicked,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50.dp),
            ) {
                Text(
                    text = stringResource(R.string.continue_text),
                    color = Color.White
                )
            }

            Text(
                text = stringResource(R.string.by_continuing_you_agree),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 2.dp)
                    .clickable {
                        openInBrowser(uriHandler, legalURL)
                    }
            )
        }
    }
}


@Composable
fun MessageBox(chat: ChatMessage) {
    val avatar = painterResource(id = chat.senderAvatar)
    val colorMatrix = ColorMatrix()
    colorMatrix.setToSaturation(0f)

    var avatarModifier = Modifier
        .size(28.dp)
        .clip(CircleShape)

    if (!chat.isUser) {
        avatarModifier = avatarModifier.graphicsLayer(
            scaleX = 1.35f,
            scaleY = 1.35f,
            rotationZ = 1f
        )
    }

    Surface(shape = RoundedCornerShape(24.dp), border = null) {
        Column(
            Modifier
                .background(
                    if (!chat.isUser) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .wrapContentWidth()
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(48.dp)
            ) {
                Image(
                    painter = avatar,
                    contentDescription = null,
                    modifier = avatarModifier,
                    colorFilter = if (chat.isUser) ColorFilter.colorMatrix(colorMatrix) else null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = chat.sender,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (!chat.isUser) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding()
                )
            }
            Text(
                text = chat.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding()
            )
        }
    }
}
