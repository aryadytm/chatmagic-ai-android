package dev.bytebooster.chatmagicai.di

import android.content.Context
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.bytebooster.chatmagicai.BuildConfig
import dev.bytebooster.chatmagicai.R
import dev.bytebooster.chatmagicai.ai.AiModelDatasource
import dev.bytebooster.chatmagicai.ai.AiModelManager
import dev.bytebooster.chatmagicai.ai.downloader.FetchAiDownloader
import dev.bytebooster.chatmagicai.ai.textgen.TextGenLoader
import dev.bytebooster.chatmagicai.data.RemoteConfigDatasource
import dev.bytebooster.chatmagicai.logDebug
import dev.bytebooster.chatmagicai.net.AnalyticsClient
import dev.bytebooster.chatmagicai.net.AnalyticsHelper
import javax.inject.Qualifier
import javax.inject.Singleton


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AndroidDownloadManager

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FetchDownloadManager


@Module
@InstallIn(SingletonComponent::class)
class AiModule {

    @Singleton
    @Provides
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val fetchInterval = if (BuildConfig.DEBUG) 60 else 3600

        logDebug("FRC fetch interval: $fetchInterval")

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = fetchInterval.toLong()
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        return remoteConfig
    }

//    @AndroidDownloadManager
//    @Singleton
//    @Provides
//    fun provideAndroidDownloader(@ApplicationContext appContext: Context): AiDownloader {
//        return AndroidAiDownloader(appContext)
//    }
//
//    @FetchDownloadManager
//    @Singleton
//    @Provides
//    fun provideFetchDownloader(@ApplicationContext appContext: Context): AiDownloader {
//        return FetchAiDownloader(appContext)
//    }

    @Singleton
    @Provides
    fun provideFetchAiDownloader(@ApplicationContext appContext: Context): FetchAiDownloader {
        return FetchAiDownloader(appContext)
    }

    @Singleton
    @Provides
    fun provideTextGenLoader(@ApplicationContext appContext: Context): TextGenLoader {
        return TextGenLoader(appContext)
    }

    @Singleton
    @Provides
    fun provideAiModelManager(
        @ApplicationContext appContext: Context,
        downloader: FetchAiDownloader,
        textGenLoader: TextGenLoader,
        modelDatasource: AiModelDatasource,
        fetchAiDownloader: FetchAiDownloader
    ): AiModelManager {
        return AiModelManager(appContext, textGenLoader, downloader, modelDatasource, fetchAiDownloader)
    }

    @Singleton
    @Provides
    fun provideRemoteConfigDatasource(
        remoteConfig: FirebaseRemoteConfig
    ): RemoteConfigDatasource {
        return RemoteConfigDatasource(remoteConfig)
    }

    @Singleton
    @Provides
    fun provideAiModelSource(
        @ApplicationContext appContext: Context,
        remoteConfigDatasource: RemoteConfigDatasource
    ): AiModelDatasource {
        return AiModelDatasource(appContext, remoteConfigDatasource)
    }

    @Singleton
    @Provides
    fun provideAnalyticsHelper(
        remoteConfigDatasource: RemoteConfigDatasource
    ): AnalyticsHelper {
        val client = AnalyticsClient(remoteConfigDatasource)
        return AnalyticsHelper(client.instance)
    }


}