package dev.bytebooster.chatmagicai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.bytebooster.chatmagicai.ai.AiModelDatasource
import dev.bytebooster.chatmagicai.ai.downloader.FetchAiDownloader
import dev.bytebooster.chatmagicai.ui.viewmodel.ThemeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var fetchAiDownloader: FetchAiDownloader
    @Inject lateinit var aiModelDatasource: AiModelDatasource

    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
        // TODO: Handle permission result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        logDebug("Creating ViewModel...")
        val themeViewModel = ThemeViewModel(this)

        logDebug("SetContent...")
        setContent {
            ChatMagicApp(themeViewModel = themeViewModel)
        }

        lifecycleScope.launch {
            delay(50)
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            view.updatePadding(bottom = bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()

        if (aiModelDatasource.isLoaded()) {
            // If remote data already loaded, resume all downloads.
            fetchAiDownloader.getFetch().resumeAll()
        }
    }

}