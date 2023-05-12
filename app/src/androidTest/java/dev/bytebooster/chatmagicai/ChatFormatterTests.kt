package dev.bytebooster.chatmagicai

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.bytebooster.chatmagicai.ai.ConvPairFormatter
import dev.bytebooster.chatmagicai.ai.TurnBasedFormatter
import dev.bytebooster.chatmagicai.ai.textgen.TextGenLoader
import dev.bytebooster.chatmagicai.ai.textgen.Tokenizer
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 *
 * TODO: Tests
 *
 * Chat
 * - The first ChatItem is from ChatMagic AI
 * - Sending a message adds new ChatItem from user
 * - Also there are incomplete message from ChatMagic AI
 * - Send message again, the 3rd index message is from User, 4th is from ChatMagic
 * - Change models must change the actual chat model
 * - When chat extends max tokens, the prompt tokens must always below max tokens
 * - Click stop button actually stops generation
 * - Click plus button also stops generation
 *
 */
@RunWith(AndroidJUnit4::class)
class ChatFormatterTests {

    lateinit var textGenLoader: TextGenLoader
    lateinit var tokenizer: Tokenizer

    val messages = listOf(
        getChatMessage(isUser = true, content = "Hello"),
        getChatMessage(isUser = false, content = "Hi there! How can I help you today?"),
        getChatMessage(isUser = true, content = "I need help with my account"),
        getChatMessage(isUser = false, content = "Sure thing! What's your account number?"),
        getChatMessage(isUser = true, content = "123456"),
    )

    @Before
    fun init() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        textGenLoader = TextGenLoader(context)
        val configJsonString = context.assets.open("modelutils-min").bufferedReader().use{ it.readText() }
        tokenizer = textGenLoader.loadTokenizer(configJsonString)
    }

    @Test
    fun testConvPairFormatter() {
        val formatter = ConvPairFormatter(maxTokens = 1800, maxMessages = 10, userPrefix = "User: ", botPrefix = "Bot: ", turnSeparator = "\n", trim = true)
        val prompt = formatter.preparePrompt(messages, tokenizer)
        val expectedPrompt =
            "User: Hello\n" +
            "Bot: Hi there! How can I help you today?\n" +
            "User: I need help with my account\n" +
            "Bot: Sure thing! What's your account number?\n" +
            "User: 123456\n" +
            "Bot:"

        assertEquals(expectedPrompt, prompt)
    }

    @Test
    fun testTurnBasedFormatter() {
        val formatter = TurnBasedFormatter(maxTokens = 1800, maxMessages = 10, turnToken = "<turn>", trim = true)
        val prompt = formatter.preparePrompt(messages, tokenizer)
        val expectedPrompt =
            "Hello<turn>" +
            "Hi there! How can I help you today?<turn>" +
            "I need help with my account<turn>" +
            "Sure thing! What's your account number?<turn>" +
            "123456<turn>"

        assertEquals(expectedPrompt, prompt)
    }

    @Test
    fun testFormatter_maxTokens() {
        val formatter = TurnBasedFormatter(maxTokens = 20, maxMessages = 10, turnToken = "<turn>", trim = true)
        val prompt = formatter.preparePrompt(messages, tokenizer)
        val expectedPrompt =
            "I need help with my account<turn>" +
            "Sure thing! What's your account number?<turn>" +
            "123456<turn>"

        assertEquals(expectedPrompt, prompt)
    }

    @Test
    fun testFormatter_maxMessages() {
        val formatter = TurnBasedFormatter(maxTokens = 1800, maxMessages = 3, turnToken = "<turn>", trim = true)
        val prompt = formatter.preparePrompt(messages, tokenizer)
        val expectedPrompt =
            "I need help with my account<turn>" +
            "Sure thing! What's your account number?<turn>" +
            "123456<turn>"

        assertEquals(expectedPrompt, prompt)
    }

    @Test
    fun testFormatter_forceUserFirst() {
        val formatter = TurnBasedFormatter(maxTokens = 1800, maxMessages = 2, turnToken = "<turn>", trim = true)
        val prompt = formatter.preparePrompt(messages, tokenizer)
        val expectedPrompt = "123456<turn>"

        assertEquals(expectedPrompt, prompt)
    }

    @Test
    fun testFormatter_maxTokens_notEmpty() {
        val formatter = TurnBasedFormatter(maxTokens = 2, maxMessages = 10, turnToken = "<turn>", trim = true)
        val prompt = formatter.preparePrompt(messages, tokenizer)

        assertEquals(true, prompt.isNotEmpty())
    }
}