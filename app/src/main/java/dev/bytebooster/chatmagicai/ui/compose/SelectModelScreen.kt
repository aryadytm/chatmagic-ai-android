package dev.bytebooster.chatmagicai.ui.compose

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.bytebooster.chatmagicai.R
import dev.bytebooster.chatmagicai.data.FakeDatasource
import dev.bytebooster.chatmagicai.model.AiModel
import dev.bytebooster.chatmagicai.ui.compose.component.ModelBox
import dev.bytebooster.chatmagicai.ui.theme.ChatMagicAITheme
import dev.bytebooster.chatmagicai.ui.viewmodel.SelectModelUiState
import dev.bytebooster.chatmagicai.ui.viewmodel.SelectModelViewModel


@Preview()
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewSelectModelScreen() {
    SelectModelAct()
}


@Composable
fun SelectModelAct() {
    ChatMagicAITheme() {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SelectModelLayout(
                uiState = SelectModelUiState(
                    startingModels = FakeDatasource().loadModels(),
                    selectedModelCard = FakeDatasource().loadModels()[0]
                ),
                onSelectedModelChanged = {},
                onContinueClicked = {}
            )
        }
    }
}


@Composable
fun SelectModelScreen(
    onContinueClicked: () -> Unit,
    selectModelViewModel: SelectModelViewModel,
) {
    LaunchedEffect(Unit) {
        selectModelViewModel.loadSelectModelPage()
    }

    val uiState by selectModelViewModel.uiState.collectAsState()

    SelectModelLayout(
        uiState = uiState,
        onSelectedModelChanged = { selectModelViewModel.updateUiState(uiState.copy(selectedModelCard = it)) },
        onContinueClicked = onContinueClicked
    )
}


@Composable
fun SelectModelLayout(
    uiState: SelectModelUiState,
    onSelectedModelChanged: (AiModel) -> Unit,
    onContinueClicked: () -> Unit,
) {
    Column(
        Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.padding(top = 24.dp))
        Text(
            text = stringResource(R.string.please_select_your),
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

        Spacer(modifier = Modifier.padding(top = 32.dp))

        uiState.startingModels.forEach {
            ModelBox(
                model = it,
                isSelected = uiState.selectedModelCard.id == it.id,
                onClick = { onSelectedModelChanged(it) }
            )
            Spacer(modifier = Modifier.padding(top = 16.dp))
        }

        Spacer(modifier = Modifier.padding(top = 16.dp))

        Text(
            text = stringResource(R.string.different_ai_models_have_different),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.padding(top = 32.dp))

        Button(
            onClick = onContinueClicked,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp)
        ) {
            Text(
                text = stringResource(R.string.continue_text),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }
    }
}
