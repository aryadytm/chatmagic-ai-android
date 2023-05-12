package dev.bytebooster.chatmagicai.net

// AnalyticsClient.kt
import dev.bytebooster.chatmagicai.model.AiModel
import dev.bytebooster.chatmagicai.model.ChatMessage
import dev.bytebooster.chatmagicai.model.net.Feedback
import dev.bytebooster.chatmagicai.model.net.Message
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AnalyticsHelper(private val analyticsClient: AnalyticsApi) {

    fun sendFeedback(chatMessages: List<ChatMessage>, model: AiModel, isPositive: Boolean, onSuccess: (Map<String, Any>) -> Unit, onError: (String) -> Unit) {
        val conversationId = generateUniqueConversationId()
        val messages = chatMessages.mapIndexed { index, chatMessage ->
            Message(
                conversation_id = conversationId,
                sender = chatMessage.sender,
                content = chatMessage.content,
                turn_index = index,
                is_user = chatMessage.isUser
            )
        }
        val feedback = Feedback(
            conversation_id = conversationId,
            model_id = model.id.toString(),
            is_positive = isPositive
        )

        addMessages(messages, { response ->
            addFeedback(feedback, { response ->
                onSuccess(response)
            }, { error ->
                onError(error)
            })
        }, { error ->
            onError(error)
        })
    }

    private fun addMessages(
        messages: List<Message>,
        onSuccess: (Map<String, Any>) -> Unit,
        onError: (String) -> Unit
    ) {
        analyticsClient.addMessages(messages).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    // Handle the successful response
                    response.body()?.let { onSuccess(it) }
                } else {
                    // Handle the error response
                    onError("Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                // Handle the failure
                onError("Failure: ${t.message}")
            }
        })
    }

    private fun addFeedback(
        feedback: Feedback,
        onSuccess: (Map<String, Any>) -> Unit,
        onError: (String) -> Unit
    ) {
        analyticsClient.addFeedback(feedback).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    // Handle the successful response
                    response.body()?.let { onSuccess(it) }
                } else {
                    // Handle the error response
                    onError("Error: ${response.body() ?: response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                // Handle the failure
                onError("Failure: ${t.message}")
            }
        })
    }

    private fun generateUniqueConversationId(): String {
        val uuid = UUID.randomUUID().toString()
        val timestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis
        return "$uuid-$timestamp"
    }
}
