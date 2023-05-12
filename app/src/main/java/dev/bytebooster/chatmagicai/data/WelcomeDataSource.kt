package dev.bytebooster.chatmagicai.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.bytebooster.chatmagicai.R
import dev.bytebooster.chatmagicai.model.ChatMessage
import javax.inject.Inject


class WelcomeDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
){

    fun getUserMessage(): ChatMessage {
        return ChatMessage(
            sender = "You",
            isUser = true,
            senderAvatar = R.drawable.ic_user,
            content = context.getString(R.string.sample_chat_user_1),
        )
    }

    fun getAiMessage(): ChatMessage {
        return ChatMessage(
            sender = "ChatMagic AI",
            isUser = false,
            senderAvatar = R.drawable.ic_chatmagicai,
            content = context.getString(R.string.sample_chat_ai_1),
        )
    }

}