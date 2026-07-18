package com.example.ava

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import com.example.ava.core.designsystem.theme.AvaTheme
import com.example.ava.core.util.updateLocale
import com.example.ava.data.local.prefs.SettingsStore
import com.example.ava.data.local.prefs.dataStore
import com.example.ava.ui.AvaAppRoot
import com.example.ava.ui.settings.AppStateViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appStateViewModel: AppStateViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        val langCode = runCatching {
            runBlocking(Dispatchers.IO) {
                newBase.dataStore.data.first()[SettingsStore.Keys.LANGUAGE] ?: "fa"
            }
        }.getOrElse { "fa" }

        val updatedContext = newBase.updateLocale(langCode)
        super.attachBaseContext(updatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var ready = false
        splash.setKeepOnScreenCondition { !ready }

        setContent {
            val settings by appStateViewModel.settings.collectAsStateWithLifecycle()
            val loggedIn by appStateViewModel.isLoggedIn.collectAsStateWithLifecycle()
            ready = true

            AvaTheme(themeMode = settings.themeMode, fontScale = settings.fontScale) {
                AvaAppRoot(isLoggedIn = loggedIn)
            }
        }
    }
}
