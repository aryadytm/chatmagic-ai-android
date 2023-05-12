package dev.bytebooster.chatmagicai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.tonyodev.fetch2.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bytebooster.chatmagicai.ai.AiModelDatasource
import dev.bytebooster.chatmagicai.ai.AiModelManager
import dev.bytebooster.chatmagicai.ai.downloader.FetchAiDownloader
import dev.bytebooster.chatmagicai.logDebug
import dev.bytebooster.chatmagicai.ui.navigation.AppPages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

/**
 * What this ViewModel do:
 * - Handles the splash screen state when loading remote data.
 * - Configures the objects that need to wait before remote data is ready.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    private val aiModelDatasource: AiModelDatasource,
    private val aiModelManager: AiModelManager,
    private val aiDownloader: FetchAiDownloader,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState(AppPages.Splash))
    val uiState = _uiState.asStateFlow()

    fun loadPage() {
        _uiState.update { it.copy(redirectRoute = AppPages.Splash) }

        viewModelScope.launch {
            logDebug("SplashViewModel load")
            startDownloadTranslator()
            loadRemoteData()
        }
    }

    private fun loadRemoteData() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    logDebug("Config params updated: $updated")
                    logDebug("Fetch and activate succeeded")
                }
                aiModelDatasource.onLoadRemoteData()
                onRemoteDataLoaded()
            }
            .addOnFailureListener {
                it.printStackTrace()
                logDebug("Fetch and activate failed!")
            }
    }

    private fun startDownloadTranslator() {

        viewModelScope.launch(Dispatchers.IO) {
            val userLanguage = Locale.getDefault().language.lowercase();
            val userCountry = getCountryCode()

            logDebug("Download Translator: userLocale = $userCountry | userLanguage = $userLanguage")

            if (userLanguage != TranslateLanguage.ENGLISH) {
                downloadTranslator(userLanguage, TranslateLanguage.ENGLISH)
            }

            if (userLanguage != userCountry && userCountry != TranslateLanguage.ENGLISH) {
                userCountry?.let {
                    downloadTranslator(it.lowercase(), TranslateLanguage.ENGLISH)
                }
            }
        }
    }

    private fun downloadTranslator(lang1: String, lang2: String) {
        val transLang1 = TranslateLanguage.fromLanguageTag(lang1)
        val transLang2 = TranslateLanguage.fromLanguageTag(lang2)

        if (transLang1 == null || transLang2 == null) {
            logDebug("Download Translator ($lang1 and $lang2) is cancelled due to null in ($transLang1, $transLang2)")
            return
        }

        // Create an English-User Locale translator:
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(transLang1)
            .setTargetLanguage(transLang2)
            .build()

        val localTranslator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder().build()

        localTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                logDebug("Success download translator: $transLang1 to $transLang2")
            }
            .addOnFailureListener { exception ->
                logDebug("Error downloading translator: $transLang1 to $transLang2: $exception")
            }
    }

    private fun onRemoteDataLoaded() {

        viewModelScope.launch(Dispatchers.IO) {
            val fetch = aiDownloader.getFetch()
            fetch.resumeAll()

            delay(2000)

            fetch.getDownloadsWithStatus(
                listOf(Status.DOWNLOADING, Status.NONE, Status.QUEUED, Status.PAUSED, Status.CANCELLED)
            ) { downloads ->

                if (downloads.isNotEmpty()) {
                    // If there are downloads, redirect to download page.
                    // No need to unuse model because people coming to download page may already have the model.
                    _uiState.update { it.copy(redirectRoute = AppPages.DownloadModel) }
                    return@getDownloadsWithStatus
                }

                val usedModel = aiModelDatasource.getUsedModel()
                val isUserCanChat = (usedModel != null) && aiModelManager.isExists(usedModel)

                if (isUserCanChat) {
                    logDebug("UsedModel: $usedModel | Redirecting to chat...")
                    // If user can chat, redirect to chat page.
                    _uiState.update { it.copy(redirectRoute = AppPages.Chat) }
                    return@getDownloadsWithStatus
                }

                // If user can't chat, redirect to welcome page. Make sure unused the model.
                aiModelDatasource.unuseModel()
                _uiState.update { it.copy(redirectRoute = AppPages.Welcome) }
            }
        }
    }

    private fun getCountryCode(): String? {

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.country.is/")
            .build()
        val response = client.newCall(request).execute()

        return if (response.isSuccessful) {
            val responseData = response.body?.string()
            val jsonObject = responseData?.let { JSONObject(it) }
            jsonObject?.getString("country")
        } else {
            null
        }
    }
}