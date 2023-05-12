package dev.bytebooster.chatmagicai.ui.compose

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import dev.bytebooster.chatmagicai.R
import dev.bytebooster.chatmagicai.data.FakeDatasource
import dev.bytebooster.chatmagicai.findActivity
import dev.bytebooster.chatmagicai.logDebug
import dev.bytebooster.chatmagicai.ui.compose.component.ModelBox
import dev.bytebooster.chatmagicai.ui.theme.ChatMagicAITheme
import dev.bytebooster.chatmagicai.ui.viewmodel.SelectModelUiState
import dev.bytebooster.chatmagicai.ui.viewmodel.SelectModelViewModel


@Preview()
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewDownloadModelScreen() {
    val context = LocalContext.current

    ChatMagicAITheme() {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            DownloadModelLayout({}, SelectModelUiState(
                    startingModels = FakeDatasource().loadModels(),
                    selectedModelCard = FakeDatasource().loadModels()[0],
                    downloadProgress = 0.7f
                )
            )
        }
    }
}

@Composable
fun DownloadModelScreen(
    onContinueClicked: () -> Unit,
    onDownloadFinished: () -> Unit,
    selectModelViewModel: SelectModelViewModel,
) {
    val uiState by selectModelViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val activity = context.findActivity()

        if (activity != null) {
            selectModelViewModel.askPostNotificationPermission(activity)
        } else {
            logDebug("WARNING: Activity is null. Cannot askPostNotificationPermission!")
        }

        selectModelViewModel.loadDownloadModelPage()
    }

    LaunchedEffect(uiState.downloadProgress) {
        if (uiState.downloadProgress >= 1f) {
            onDownloadFinished()
        }
    }

    DownloadModelLayout(
        onContinueClicked = onContinueClicked,
        uiState = uiState
    )
}


@Composable
fun DownloadModelLayout(
    onContinueClicked: () -> Unit,
    uiState: SelectModelUiState,
) {

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lt_downloading))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    Column(
        Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.padding(top = 24.dp))
        Text(
            text = stringResource(R.string.downloading_the),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
        Text(
            text = stringResource(R.string.ai_model),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)

        )

        Spacer(modifier = Modifier.padding(top = 8.dp))

        // Lottie here
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth()
                .alpha(0.75f)
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                alignment = Alignment.Center,
                modifier = Modifier
                    .size(256.dp)
            )
        }

        Spacer(modifier = Modifier.padding(top = 8.dp))

        Text(
            text = stringResource(R.string.downloading),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.padding(top = 8.dp))

        LinearProgressIndicator(
            progress = uiState.downloadProgress,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.padding(top = 32.dp))


        ModelBox(
            model = uiState.selectedModelCard,
            isSelected = true,
            onClick = {}
        )

        Spacer(modifier = Modifier.padding(top = 32.dp))

        Button(
            onClick = onContinueClicked,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp),
            enabled = uiState.downloadProgress >= 1f,
        ) {
            Text(
                text = stringResource(R.string.continue_text),
                color = Color.White
            )
        }
    }
}