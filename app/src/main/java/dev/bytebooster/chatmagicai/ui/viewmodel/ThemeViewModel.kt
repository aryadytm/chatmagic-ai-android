package dev.bytebooster.chatmagicai.ui.viewmodel

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bytebooster.chatmagicai.MainActivity
import dev.bytebooster.chatmagicai.data.PrefManager
import dev.bytebooster.chatmagicai.logDebug
import dev.bytebooster.chatmagicai.ui.theme.md_theme_dark_background
import dev.bytebooster.chatmagicai.ui.theme.md_theme_light_background
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * NOTE: This class is bound into activity's lifecycle.
 * Don't treat this class same like ordinary ViewModels
 */
class ThemeViewModel constructor(
    activityContext: Context
) : ViewModel() {

    companion object {
        const val PREFNAME_MENUVIEWMODEL = "menuviewmodel"
        const val PREFKEY_APPDARKMODE = "isdarkmode"
        enum class AppDarkModeState { UNSET, DARK, LIGHT }
    }

    private val prefManager = PrefManager(activityContext, PREFNAME_MENUVIEWMODEL)
    private val resources = activityContext.resources
    private val window = (activityContext as MainActivity).window

    private val _uiState = MutableStateFlow(
        ThemeUiState(
            isDarkMode = false
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            var appDarkMode = prefManager.getString(PREFKEY_APPDARKMODE, AppDarkModeState.UNSET.name)

            if (appDarkMode == AppDarkModeState.UNSET.name) {
                // App dark mode is unset. Use system instead.
                val systemDarkModeState = if (getSystemDarkMode()) AppDarkModeState.DARK else AppDarkModeState.LIGHT
                appDarkMode = systemDarkModeState.name
                prefManager.setString(PREFKEY_APPDARKMODE, systemDarkModeState.name)
            }

            // Set dark mode toggle according to saved option
            val useDarkMode = appDarkMode == AppDarkModeState.DARK.name
            _uiState.update {
                it.copy(isDarkMode = useDarkMode)
            }

            updateSystemUiTheme()
            logDebug("Dark Mode: ${uiState.value}")
        }
    }

    fun toggleDarkMode(darkMode: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isDarkMode = darkMode)
            }
            val appDarkModeState = if (uiState.value.isDarkMode) AppDarkModeState.DARK else AppDarkModeState.LIGHT
            prefManager.setString(PREFKEY_APPDARKMODE, appDarkModeState.name)

            updateSystemUiTheme()
            logDebug("Toggle Dark Mode to: $darkMode")
        }
    }

    private fun updateSystemUiTheme() {
        viewModelScope.launch {
            val color = if (uiState.value.isDarkMode) md_theme_dark_background else md_theme_light_background
            window.statusBarColor = color.toArgb()
            window.navigationBarColor = color.toArgb()

            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = !uiState.value.isDarkMode
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !uiState.value.isDarkMode
        }
    }


    private fun getSystemDarkMode(): Boolean {
        // Correct the dark mode toggle in UI state
        val nightModeFlags: Int = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> _uiState.update { return true }
            Configuration.UI_MODE_NIGHT_NO -> _uiState.update { return false }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> _uiState.update { return false }
        }
        return false
    }

}