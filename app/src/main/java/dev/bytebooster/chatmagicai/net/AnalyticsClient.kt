package dev.bytebooster.chatmagicai.net

import dev.bytebooster.chatmagicai.data.RemoteConfigDatasource
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AnalyticsClient(private val remoteConfigDatasource: RemoteConfigDatasource) {

    private val defaultBaseUrl = "https://chatmagic-ai.vercel.app"
    private val baseUrl = remoteConfigDatasource.getAnalyticsBaseUrl() ?: defaultBaseUrl

    private val client = OkHttpClient.Builder()
        .build()

    val instance: AnalyticsApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        retrofit.create(AnalyticsApi::class.java)
    }
}