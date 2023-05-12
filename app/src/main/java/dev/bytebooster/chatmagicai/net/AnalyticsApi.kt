package dev.bytebooster.chatmagicai.net

import dev.bytebooster.chatmagicai.model.net.Feedback
import dev.bytebooster.chatmagicai.model.net.Message
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AnalyticsApi {
    @POST("api/add-messages")
    fun addMessages(@Body messages: List<Message>): Call<Map<String, Any>>

    @POST("api/add-feedback")
    fun addFeedback(@Body feedback: Feedback): Call<Map<String, Any>>
}