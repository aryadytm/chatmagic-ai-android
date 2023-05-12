package dev.bytebooster.chatmagicai.ui.compose

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alorma.compose.settings.storage.base.rememberBooleanSettingState
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import dev.bytebooster.chatmagicai.R
import dev.bytebooster.chatmagicai.openInBrowser
import dev.bytebooster.chatmagicai.ui.compose.component.TopBackButton
import dev.bytebooster.chatmagicai.ui.theme.ChatMagicAITheme
import dev.bytebooster.chatmagicai.ui.viewmodel.MenuViewModel
import dev.bytebooster.chatmagicai.ui.viewmodel.ThemeViewModel


@Preview()
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewMenuScreen() {
    MenuAct()
}


@Composable
fun MenuAct() {
    ChatMagicAITheme() {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MenuScreen(
                onBackClicked = {},
                onChangeAiModelClicked = {},
                onChatHistoryClicked = {},
                onDeleteAiModelClicked = {},
                themeViewModel = hiltViewModel()
            )
        }
    }
}


@Composable
fun MenuScreen(
    onBackClicked: () -> Unit,
    onChatHistoryClicked: () -> Unit,
    onChangeAiModelClicked: () -> Unit,
    onDeleteAiModelClicked: () -> Unit,
    themeViewModel: ThemeViewModel,
    menuViewModel: MenuViewModel = hiltViewModel()
) {
    val themeState by themeViewModel.uiState.collectAsState()
    val darkModeState = rememberBooleanSettingState(defaultValue = themeState.isDarkMode)
    val ctx = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val appIcon = painterResource(id = R.drawable.ic_chatmagicai)

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

        TopBackButton(onBackClicked = onBackClicked)

        Spacer(modifier = Modifier.padding(top = 16.dp))
        Image(
            painter = appIcon,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .size(128.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.padding(top = 16.dp))
        Text(
            text = stringResource(R.string.my_app_name),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.padding(top = 16.dp))

        Divider(
            color = MaterialTheme.colorScheme.secondaryContainer,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        /******************
         * App menu section
         *****************
         */

        SettingsMenuLink(
            icon = { Icon(painterResource(R.drawable.baseline_chat_24), null) },
            title = { Text(text = stringResource(R.string.back_to_my_chat)) },
            onClick = onBackClicked,
        )

        SettingsMenuLink(
            icon = { Icon(painterResource(R.drawable.baseline_history_24), null) },
            title = { Text(text = stringResource(R.string.chat_history)) },
            onClick = onChatHistoryClicked,
        )

        Divider(
            color = MaterialTheme.colorScheme.secondaryContainer,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        /******************
         * Settings section
         *****************
         */

        SettingsSwitch(
            icon = { Icon(painterResource(R.drawable.baseline_dark_mode_24), null) },
            title = { Text(text = stringResource(R.string.dark_mode)) },
            state = darkModeState,
            onCheckedChange = { themeViewModel.toggleDarkMode(it) },
        )


        SettingsMenuLink(
            icon = { Icon(painterResource(R.drawable.baseline_drive_file_move_24), null) },
            title = { Text(text = stringResource(R.string.change_ai_model)) },
            onClick = onChangeAiModelClicked,
        )

        SettingsMenuLink(
            icon = { Icon(painterResource(R.drawable.baseline_delete_24), null) },
            title = { Text(text = stringResource(R.string.delete_ai_model)) },
            onClick = onDeleteAiModelClicked,
        )

        Divider(
            color = MaterialTheme.colorScheme.secondaryContainer,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        /******************
         * Legal section
         *****************
         */

        SettingsMenuLink(
            icon = { Icon(painterResource(R.drawable.baseline_feedback_24), null) },
            title = { Text(text = stringResource(R.string.send_feedback)) },
            onClick = { openInBrowser(uriHandler, menuViewModel.feedbackUrl) },
        )
        SettingsMenuLink(
            icon = { Icon(painterResource(R.drawable.baseline_mail_24), null) },
            title = { Text(text = stringResource(R.string.contact_us)) },
            onClick = { openInBrowser(uriHandler, menuViewModel.contactUrl) },
        )
        SettingsMenuLink(
            icon = { Icon(painterResource(R.drawable.baseline_privacy_tip_24), null) },
            title = { Text(text = stringResource(R.string.privacy_policy)) },
            onClick = { openInBrowser(uriHandler, menuViewModel.privacyPolicyUrl) },
        )
        SettingsMenuLink(
            icon = { Icon(painterResource(R.drawable.baseline_warning_24), null) },
            title = { Text(text = stringResource(R.string.terms_of_service)) },
            onClick = { openInBrowser(uriHandler, menuViewModel.termsOfServiceUrl) },
        )

        Divider(color = MaterialTheme.colorScheme.secondaryContainer, thickness = 1.dp)
    }
}