package dev.bytebooster.chatmagicai.ui.compose

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.bytebooster.chatmagicai.data.FakeDatasource
import dev.bytebooster.chatmagicai.model.Template
import dev.bytebooster.chatmagicai.ui.theme.ChatMagicAITheme


@Preview()
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewTemplatesAct() {
    TemplatesAct()
}


@Composable
fun TemplatesAct() {
    ChatMagicAITheme() {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            TemplatesScreen()
        }
    }
}


@Composable
fun TemplatesScreen() {
    val templates = FakeDatasource().loadTemplates()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(templates) {
            TemplateItem(template = it)
        }
    }

}


@Composable
fun TemplateItem(template: Template, modifier: Modifier = Modifier) {
    val thumbnail = painterResource(id = template.image)
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column() {
            Image(
                painter = thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Fit,
            )
            Text(
                text = template.title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)
            )
            Text(
                text = template.desc,
                modifier = Modifier
                    .padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 8.dp)
                    .fillMaxHeight()
            )
        }
    }
}
