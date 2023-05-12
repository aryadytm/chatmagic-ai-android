package dev.bytebooster.chatmagicai.viewmodel

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.bytebooster.chatmagicai.ai.AiModelDatasource
import dev.bytebooster.chatmagicai.ai.AiModelManager
import dev.bytebooster.chatmagicai.data.RemoteConfigDatasource
import dev.bytebooster.chatmagicai.model.GenerationParameters
import dev.bytebooster.chatmagicai.net.AnalyticsHelper
import dev.bytebooster.chatmagicai.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * IMPORTANT NOTE:
 * Before running this test, make sure the TEST_MODEL_ID is already downloaded!
 */
@HiltAndroidTest
class ChatViewModelTests {

    private val TEST_MODEL_ID = 2023031101

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject lateinit var aiModelManager: AiModelManager
    @Inject lateinit var aiModelDatasource: AiModelDatasource
    @Inject lateinit var remoteConfigDatasource: RemoteConfigDatasource
    @Inject lateinit var analyticsHelper: AnalyticsHelper

    private lateinit var chatViewModel: ChatViewModel

    private fun waitUntilStopGenerating() {

        for (i in 0..20)
            if (!chatViewModel.uiState.value.isGenerating)
                break
            else
                Thread.sleep(200)

        Thread.sleep(200)
    }

    @Before
    fun init() {

        hiltRule.inject()

        runBlocking {
            Firebase.remoteConfig.fetchAndActivate().await()
        }

        aiModelDatasource.onLoadRemoteData()
        aiModelDatasource.saveAsUsedModel(aiModelDatasource.getModelById(TEST_MODEL_ID)!!)

        val generationParameters = GenerationParameters(
            doSample = false, temperature = 0.1f, topK = 1, topP = 1.0f, repetitionPenalty = 1.0f, maxNewTokens = 10,
        )
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        chatViewModel = ChatViewModel(aiModelManager, aiModelDatasource, remoteConfigDatasource, analyticsHelper, context)
        chatViewModel.loadPage()
        chatViewModel.setGenerationParameters(generationParameters)

        while (chatViewModel.uiState.value.isLoading)
            Thread.sleep(100)
    }


    @Test
    fun test_initial_state() {
        chatViewModel.clickNewChat()

        assertThat(chatViewModel.uiState.value.messages).hasSize(1)
        assertThat(chatViewModel.uiState.value.isGenerating).isFalse()
    }

    @Test
    fun test_click_new_chat_after_send_message__success() {
        chatViewModel.updateUiState(chatViewModel.uiState.value.copy(chatTextFieldValue = "Hello"))
        chatViewModel.clickSendMessage()
        waitUntilStopGenerating()

        chatViewModel.clickNewChat()

        val messages = chatViewModel.uiState.value.messages

        assertThat(messages).hasSize(1)
        assertThat(chatViewModel.uiState.value.isGenerating).isFalse()
    }

    @Test
    fun test_click_send_message_with_empty_input__message_not_sent() {
        chatViewModel.clickNewChat()

        chatViewModel.updateUiState(chatViewModel.uiState.value.copy(chatTextFieldValue = ""))
        chatViewModel.clickSendMessage()
        waitUntilStopGenerating()

        val messages = chatViewModel.uiState.value.messages
        assertThat(messages).hasSize(1)
        assertThat(chatViewModel.uiState.value.isGenerating).isFalse()
    }

    @Test
    fun test_click_send_message_with_non_empty_input__message_sent() {
        chatViewModel.clickNewChat()

        chatViewModel.updateUiState(chatViewModel.uiState.value.copy(chatTextFieldValue = "Hello"))
        chatViewModel.clickSendMessage()
        assertThat(chatViewModel.uiState.value.isGenerating).isTrue()
        waitUntilStopGenerating()

        val messages = chatViewModel.uiState.value.messages

        assertThat(messages).hasSize(3)
        assertThat(messages[1].content).isEqualTo("Hello")
        assertThat(messages[1].isUser).isTrue()
        assertThat(messages[2].isUser).isFalse()
    }

    @Test
    fun test_click_regenerate() {
        chatViewModel.clickNewChat()

        chatViewModel.updateUiState(chatViewModel.uiState.value.copy(chatTextFieldValue = "Hello"))

        chatViewModel.clickSendMessage()
        assertThat(chatViewModel.uiState.value.isGenerating).isTrue()
        waitUntilStopGenerating()
        assertThat(chatViewModel.uiState.value.isGenerating).isFalse()

        chatViewModel.clickRegenerate()
        assertThat(chatViewModel.uiState.value.isGenerating).isTrue()
        waitUntilStopGenerating()
        assertThat(chatViewModel.uiState.value.isGenerating).isFalse()

        val messages = chatViewModel.uiState.value.messages

        assertThat(messages).hasSize(3)
        assertThat(messages[1].content).isEqualTo("Hello")
        assertThat(messages[1].isUser).isTrue()
        assertThat(messages[2].isUser).isFalse()
    }

    @Test
    fun test_click_feedback() {
        chatViewModel.clickNewChat()

        chatViewModel.updateUiState(chatViewModel.uiState.value.copy(chatTextFieldValue = "Hello"))

        chatViewModel.clickSendMessage()
        waitUntilStopGenerating()
        chatViewModel.clickFeedback(isPositive = true)

        val uiState = chatViewModel.uiState.value
        assertThat(uiState.showFeedbackButtons).isFalse()
    }

    @Test
    fun test_click_stop_generate() {
        chatViewModel.clickNewChat()

        chatViewModel.updateUiState(chatViewModel.uiState.value.copy(chatTextFieldValue = "Hello"))

        chatViewModel.clickSendMessage()
        assertThat(chatViewModel.uiState.value.isGenerating).isTrue()
        chatViewModel.clickSendMessage()
        assertThat(chatViewModel.uiState.value.isGenerating).isFalse()
    }

    @Test
    fun test_click_new_chat_button__stop_generate() {
        chatViewModel.clickNewChat()

        chatViewModel.updateUiState(chatViewModel.uiState.value.copy(chatTextFieldValue = "Hello"))

        chatViewModel.clickSendMessage()
        assertThat(chatViewModel.uiState.value.isGenerating).isTrue()
        chatViewModel.clickNewChat()
        assertThat(chatViewModel.uiState.value.isGenerating).isFalse()
    }

    @Test
    fun test_multi_turn_chat() {
        val msg1 = "who is elon musk?"
        val msg2 = "what is his net worth?"

        chatViewModel.clickNewChat()

        chatViewModel.updateUiState(chatViewModel.uiState.value.copy(chatTextFieldValue = msg1))
        chatViewModel.clickSendMessage()
        waitUntilStopGenerating()

        chatViewModel.updateUiState(chatViewModel.uiState.value.copy(chatTextFieldValue = msg2))
        chatViewModel.clickSendMessage()
        waitUntilStopGenerating()

        // Check the chatViewModel.uiState.value.messages to make sure the messages are actual
        assertThat(chatViewModel.uiState.value.messages).hasSize(5)
        assertThat(chatViewModel.uiState.value.messages[1].isUser).isTrue()
        assertThat(chatViewModel.uiState.value.messages[1].content).isEqualTo(msg1)
        assertThat(chatViewModel.uiState.value.messages[2].isUser).isFalse()
        assertThat(chatViewModel.uiState.value.messages[2].content.lowercase()).contains("elon")
        assertThat(chatViewModel.uiState.value.messages[3].isUser).isTrue()
        assertThat(chatViewModel.uiState.value.messages[3].content).isEqualTo(msg2)
        assertThat(chatViewModel.uiState.value.messages[4].isUser).isFalse()
    }


}