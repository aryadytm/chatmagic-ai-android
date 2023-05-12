package dev.bytebooster.chatmagicai

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.bytebooster.chatmagicai.ui.navigation.ChatMagicNavHost
import dev.bytebooster.chatmagicai.ui.theme.ChatMagicAITheme
import dev.bytebooster.chatmagicai.ui.viewmodel.ThemeViewModel

@Composable
fun ChatMagicApp(
    navController: NavHostController = rememberNavController(),
    themeViewModel: ThemeViewModel
) {
    val themeState by themeViewModel.uiState.collectAsState()

    ChatMagicAITheme(useDarkTheme = themeState.isDarkMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ChatMagicNavHost(
                navController = navController,
                themeViewModel = themeViewModel,
            )
        }
    }
}