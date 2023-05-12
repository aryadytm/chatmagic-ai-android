package dev.bytebooster.chatmagicai.ui.compose.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TopBackButton(onBackClicked: () -> Unit) {
    IconButton(onClick = onBackClicked, modifier = Modifier) {
        Icon(
            Icons.Default.ArrowBack,
            null,
            tint = MaterialTheme.colorScheme.onBackground,

            )
    }
}