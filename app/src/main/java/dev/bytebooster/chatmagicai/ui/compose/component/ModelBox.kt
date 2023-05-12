package dev.bytebooster.chatmagicai.ui.compose.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bytebooster.chatmagicai.model.AiModel


@Composable
fun ModelBox(model: AiModel, isSelected: Boolean, showDescription: Boolean = true, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        border = null,
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        Column(
            Modifier
                .background(if (!isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary)
                .wrapContentWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = model.name,
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 14.sp,
                color = if (!isSelected) MaterialTheme.colorScheme.onBackground else Color.White,
                modifier = Modifier.padding().fillMaxWidth()
            )

            if (showDescription) {
                Text(
                    text = model.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = if (!isSelected) MaterialTheme.colorScheme.onBackground else Color.White,
                    modifier = Modifier.padding()
                )
            }

            Text(
                text = "Version: ${model.id}",
                style = MaterialTheme.typography.bodySmall,
                color = if (!isSelected) MaterialTheme.colorScheme.onBackground else Color.White,
                modifier = Modifier
                    .alpha(0.5f)
                    .padding(top = 2.dp)
            )
        }
    }
}