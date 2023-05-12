package dev.bytebooster.chatmagicai.ai

import dev.bytebooster.chatmagicai.R
import dev.bytebooster.chatmagicai.ai.textgen.Tokenizer
import dev.bytebooster.chatmagicai.model.ChatMessage


// TODO: Create unit tests for this.

abstract class ChatFormatter(
    private val maxTokens: Int,
    private val maxMessages: Int,
    // maxMessages of 1 means only user message.
    // Odd number maxMessages means the user is always the first sender.
) {

    fun preparePrompt(messages: List<ChatMessage>, tokenizer: Tokenizer): String {
        var totalTokens = 0
        val lastMessages = mutableListOf<ChatMessage>()

        for (message in messages.reversed()) {
            val messageTokens = tokenizer.encode(message.content).size
            if (totalTokens + messageTokens <= maxTokens) {
                lastMessages.add(message)
                totalTokens += messageTokens
            } else {
                break
            }
        }

        lastMessages.reverse()

        if (lastMessages.size == 0) {
            // TODO: Fix this hacky way in case the user's prompt too long so there are no messages.
            lastMessages.add(ChatMessage(
                sender = "You",
                senderAvatar = R.drawable.ic_user,
                isUser = true,
                // TODO: Make a dataset for this.
                content = "Please write a message that says \"Your message is too long for me to digest. Please reset the chat and send a shorter message.\"",
            ))
        }

        val selectedMessages = if (lastMessages.size > maxMessages) {
            lastMessages.subList(lastMessages.size - maxMessages, lastMessages.size)
        } else {
            lastMessages
        }

        // TODO: Should we only support "User-AI" message pair? Depending the trained model dataset.
        // Currently, if the first message is not from user, we remove them.
        // TODO: The "TurnBasedFormatter" may support that kind of conversation. Please research.
        if (selectedMessages.size >= 2 && !selectedMessages[0].isUser) {
            selectedMessages.removeAt(0)
        }

        return formatChatToPrompt(selectedMessages)
    }

    protected abstract fun formatChatToPrompt(chat: List<ChatMessage>): String
}


class ConvPairFormatter(
    maxTokens: Int,
    maxMessages: Int,
    private val userPrefix: String,
    private val botPrefix: String,
    private val turnSeparator: String,
    private val trim: Boolean,
) : ChatFormatter(maxTokens = maxTokens, maxMessages = maxMessages) {

    /**
     * Parameters:
     * ```
     * userPrefix is "Human:\n" | botPrefix is "Assistant:\n" | userBotSeparator is "\n\n"
     * userMessage is "Who is Elon Musk?" | botMessage is "Elon Musk is an entrepreneur."
     * ```
     * Example using parameters above. Suppose we have three chats, the human just clicked the send message button
     * ```
     * ChatMessage(isUser=true, content="Who is Elon Musk?")
     * ChatMessage(isUser=false, content="Elon Musk is an entrepreneur.")
     * ChatMessage(isUser=true, content="When does he born?")
     * ```
     * Here is the formatted string
     * ```text
     * Human:
     * Who is Elon Musk?
     *
     * Assistant:
     * Elon Musk is an entrepreneur.
     *
     * Human:
     * When does he born?
     *
     * Assistant:
     * (\n)
     * ```
     *
     */
    override fun formatChatToPrompt(chat: List<ChatMessage>): String {
        var formattedString = ""

        for (msg in chat) {
            formattedString += formatMessage(msg.content, msg.isUser)
            formattedString += turnSeparator
        }

        // Inject empty bot message
        formattedString += formatMessage(text = "", isUser = false)

        if (trim) {
            formattedString = formattedString.trim()
        }

        return formattedString
    }

    private fun formatMessage(text: String, isUser: Boolean): String {
        val prefix = if (isUser) userPrefix else botPrefix
        return prefix + text
    }

}

class TurnBasedFormatter(
    maxTokens: Int,
    maxMessages: Int,
    private val turnToken: String,
    private val trim: Boolean,
    ) : ChatFormatter(maxTokens = maxTokens, maxMessages = maxMessages) {

    /**
     * Parameters:
     * ```
     * turnToken = "<|eot|>\n"
     * ```
     * Example using parameters above. Suppose we have three chats, the human just clicked the send message button
     * ```
     * ChatMessage(isUser=true, content="Who is Elon Musk?")
     * ChatMessage(isUser=false, content="Elon Musk is an entrepreneur.")
     * ChatMessage(isUser=true, content="When does he born?")
     * ```
     * Here is the formatted string
     * ```text
     * Who is Elon Musk?<|eot|>
     * Elon Musk is an entrepreneur.<|eot|>
     * When does he born?<|eot|>
     * (\n)
     * ```
     *
     */
    override fun formatChatToPrompt(chat: List<ChatMessage>): String {
        var formattedString = ""

        for (msg in chat) {
            formattedString += msg.content + turnToken
        }

        if (trim) {
            formattedString = formattedString.trim()
        }

        return formattedString
    }

}