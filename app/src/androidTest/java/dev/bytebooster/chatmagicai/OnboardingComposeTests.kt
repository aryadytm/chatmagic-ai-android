package dev.bytebooster.chatmagicai

import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.bytebooster.chatmagicai.ai.AiModelDatasource
import dev.bytebooster.chatmagicai.ai.AiModelManager
import dev.bytebooster.chatmagicai.ui.navigation.AppPages
import dev.bytebooster.chatmagicai.ui.theme.ChatMagicAITheme
import dev.bytebooster.chatmagicai.ui.viewmodel.ThemeViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

//@CustomTestApplication(BaseApp::class)
//interface HiltTestApplication
//

@HiltAndroidTest
class OnboardingComposeTests {

    @get:Rule var hiltRule = HiltAndroidRule(this)
    @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject lateinit var aiModelManager: AiModelManager
    @Inject lateinit var aiModelDatasource: AiModelDatasource

    lateinit var navController: TestNavHostController

    @Before
    fun init() {
        hiltRule.inject()

        runBlocking {
            Firebase.remoteConfig.fetchAndActivate().await()
        }
        aiModelDatasource.onLoadRemoteData()

        val usedModel = aiModelDatasource.getUsedModel()

        if (usedModel != null) {
            aiModelManager.delete(usedModel)
            aiModelDatasource.unuseModel()
        }

        val themeViewModel = ThemeViewModel(composeTestRule.activity)

        composeTestRule.activity.setContent {
            ChatMagicAITheme() {
                navController = TestNavHostController(LocalContext.current)
                navController.navigatorProvider.addNavigator(ComposeNavigator())
                ChatMagicApp(themeViewModel = themeViewModel, navController = navController)
            }
        }
    }

    @Test
    fun testOnboarding() {
        // Start the app
        composeTestRule.waitUntil(timeoutMillis = 10_000) { this::navController.isInitialized }
        composeTestRule.waitUntil(timeoutMillis = 10_000) { navController.currentDestination?.route == AppPages.Welcome.name }

        composeTestRule.onNodeWithText("Continue").performClick()

        composeTestRule.waitUntil(timeoutMillis = 2_000) { navController.currentDestination?.route == AppPages.SelectModel.name }
    }
}