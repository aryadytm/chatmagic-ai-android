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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.bytebooster.chatmagicai.R
import dev.bytebooster.chatmagicai.data.FakeDatasource
import dev.bytebooster.chatmagicai.ui.compose.component.ModelBox
import dev.bytebooster.chatmagicai.ui.compose.component.TopBackButton
import dev.bytebooster.chatmagicai.ui.theme.ChatMagicAITheme
import dev.bytebooster.chatmagicai.ui.viewmodel.DeleteModelUiState
import dev.bytebooster.chatmagicai.ui.viewmodel.DeleteModelViewModel


@Preview()
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewDeleteModelScreen() {
    ChatMagicAITheme() {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            DeleteModelLayout(
                uiState = DeleteModelUiState(
                    downloadedModels = FakeDatasource().loadModels(),
                    selectedModelIndex = 0
                ),
                onSelectedModelChanged = {},
                onBackClicked = {},
                onDeleteClicked = {}
            )
        }
    }
}


@Composable
fun DeleteModelScreen(
    deleteModelViewModel: DeleteModelViewModel = hiltViewModel(),
    onBackClicked: () -> Unit,
) {
    val uiState by deleteModelViewModel.uiState.collectAsState()

    Column {
        TopBackButton(onBackClicked = onBackClicked)
        DeleteModelLayout(
            uiState = uiState,
            onSelectedModelChanged = {
                deleteModelViewModel.onSelectedModelChanged(it)
            },
            onBackClicked = onBackClicked,
            onDeleteClicked = {
                deleteModelViewModel.deleteModel()
                onBackClicked()
            }
        )
    }
}


@Composable
fun DeleteModelLayout(
    uiState: DeleteModelUiState,
    onBackClicked: () -> Unit,
    onSelectedModelChanged: (Int) -> Unit,
    onDeleteClicked: () -> Unit,
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
            text = stringResource(R.string.ai_model_to_delete),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)

        )

        Spacer(modifier = Modifier.padding(top = 32.dp))

        uiState.downloadedModels.forEachIndexed { idx, it ->
            ModelBox(
                model = it,
                isSelected = uiState.selectedModelIndex == idx,
                showDescription = false,
                onClick = { onSelectedModelChanged(idx) }
            )
            Spacer(modifier = Modifier.padding(top = 16.dp))
        }

        Spacer(modifier = Modifier.padding(top = 8.dp))

        Text(
            text = stringResource(R.string.keep_in_mind_delete),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .padding(horizontal = 8.dp)

        )

        Spacer(modifier = Modifier.padding(top = 8.dp))

        Button(
            onClick = onDeleteClicked,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp)
        ) {
            Text(
                text = stringResource(R.string.delete_text),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }
    }
}
