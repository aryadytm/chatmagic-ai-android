package dev.bytebooster.chatmagicai.ui.navigation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.bytebooster.chatmagicai.R
import dev.bytebooster.chatmagicai.ui.compose.*
import dev.bytebooster.chatmagicai.ui.viewmodel.ChatViewModel
import dev.bytebooster.chatmagicai.ui.viewmodel.SelectModelViewModel
import dev.bytebooster.chatmagicai.ui.viewmodel.SplashViewModel
import dev.bytebooster.chatmagicai.ui.viewmodel.ThemeViewModel
import kotlinx.coroutines.delay


enum class AppPages {
    Splash,
    Welcome,
    SelectModel,
    DownloadModel,
    DeleteModel,
    Chat,
    Menu,
    Template,
    ComingSoon,
}


@Composable
fun ChatMagicNavHost(
    navController: NavHostController,
    themeViewModel: ThemeViewModel,
    splashViewModel: SplashViewModel = hiltViewModel(),
    selectModelViewModel: SelectModelViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
    ) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = AppPages.valueOf(
        backStackEntry?.destination?.route ?: AppPages.Welcome.name
    )

    // Wait in splash screen until mandatory remote data finishes
    val startDestination = AppPages.Splash.name

    LaunchedEffect(Unit) {
        navController.navigate(startDestination)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier
    ) {
        composable(route = AppPages.Splash.name) {
            SplashScreen(
                splashViewModel = splashViewModel,
                onLoaded = { destinationAfterLoad ->
                    // Load data for select model screen (after remote data loaded)
                    selectModelViewModel.loadSelectModelPage()
                    // Redirect user to chat screen if they already downloaded a model
                    navController.navigate(destinationAfterLoad.name) {
                        // Clear back stack to splash page
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                    navController.clearBackStack(destinationAfterLoad.name)
                }
            )
        }
        composable(route = AppPages.Welcome.name) {
            WelcomeScreen(
                onContinueClicked = {
                    navController.navigate(AppPages.SelectModel.name)
                }
            )
        }
        composable(route = AppPages.SelectModel.name) {
            SelectModelScreen(
                onContinueClicked = {
                    navController.navigate(AppPages.DownloadModel.name) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                    // Disable go back when downloading the model
                    navController.clearBackStack(AppPages.DownloadModel.name)
                },
                selectModelViewModel = selectModelViewModel
            )
        }
        composable(route = AppPages.DownloadModel.name) {
            DownloadModelScreen(
                onContinueClicked = {
                    navigateToChat(navController)
                },
                onDownloadFinished = {
                    navigateToChat(navController)
                },
                selectModelViewModel = selectModelViewModel
            )
        }
        composable(route = AppPages.DeleteModel.name) {
            DeleteModelScreen(
                onBackClicked = { navController.navigateUp() }
            )
        }
        composable(route = AppPages.Chat.name) {
            ChatScreen(
                onMenuIconClicked = {
                    navController.navigate(AppPages.Menu.name)
                },
                onEmptyModel = { /* TODO: Should we navigate to select model? */ },
                chatViewModel = chatViewModel,
            )
            DoubleBackToExit()
        }
        composable(route = AppPages.Menu.name) {
            MenuScreen(
                themeViewModel = themeViewModel,
                onBackClicked = {
                    navController.navigateUp()
                },
                onChatHistoryClicked = {
                    navController.navigate(AppPages.ComingSoon.name)
                },
                onChangeAiModelClicked = {
                    navController.navigate(AppPages.SelectModel.name)
                },
                onDeleteAiModelClicked = {
                    navController.navigate(AppPages.DeleteModel.name)
                },
            )
        }
        composable(route = AppPages.ComingSoon.name) {
            ComingSoonScreen(
                onBackClicked = {
                    navController.navigateUp()
                }
            )
        }
    }
}

private fun navigateToChat(navController: NavHostController) {
    navController.navigate(AppPages.Chat.name) {
        popUpTo(navController.graph.id) { inclusive = true }
    }
    // Disable go back to boarding screens because no longer need to download.
    // TODO: Show dialog whether to exit app
    navController.clearBackStack(AppPages.Chat.name)
}

sealed class BackPress {
    object Idle : BackPress()
    object InitialTouch : BackPress()
}


@Composable
private fun DoubleBackToExit() {
    var showToast by remember { mutableStateOf(false) }

    var backPressState by remember { mutableStateOf<BackPress>(BackPress.Idle) }
    val context = LocalContext.current

    if (showToast) {
        Toast.makeText(context, stringResource(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show()
        showToast= false
    }


    LaunchedEffect(key1 = backPressState) {
        if (backPressState == BackPress.InitialTouch) {
            delay(2000)
            backPressState = BackPress.Idle
        }
    }

    BackHandler(backPressState == BackPress.Idle) {
        backPressState = BackPress.InitialTouch
        showToast = true
    }
}