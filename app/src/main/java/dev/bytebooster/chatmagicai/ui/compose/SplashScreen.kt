package dev.bytebooster.chatmagicai.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.bytebooster.chatmagicai.R
import dev.bytebooster.chatmagicai.ui.navigation.AppPages
import dev.bytebooster.chatmagicai.ui.theme.ChatMagicAITheme
import dev.bytebooster.chatmagicai.ui.viewmodel.SplashViewModel


@Composable
@Preview
fun PreviewSplashScreen() {
    ChatMagicAITheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SplashScreenLayout()
        }
    }
}


@Composable
fun SplashScreen(
    splashViewModel: SplashViewModel,
    onLoaded: (AppPages) -> Unit
) {
    val uiState by splashViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        splashViewModel.loadPage()
    }

    LaunchedEffect(uiState.redirectRoute) {
        if (uiState.redirectRoute != AppPages.Splash)
            onLoaded(uiState.redirectRoute)
    }

    SplashScreenLayout()
}

@Composable
fun SplashScreenLayout() {
    val appIcon = painterResource(id = R.drawable.ic_chatmagicai)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        Image(
            painter = appIcon,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .size(128.dp)
                .clip(CircleShape)
                .graphicsLayer(
                    scaleX = 1.2f,
                    scaleY = 1.2f,
                    rotationZ = 1f
                )
        )
        Spacer(modifier = Modifier.padding(top = 16.dp))
        Text(
            text = stringResource(id = R.string.my_app_name),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
    }
}