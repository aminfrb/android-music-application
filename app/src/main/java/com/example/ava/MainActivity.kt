package com.example.ava

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import com.example.ava.core.designsystem.theme.AvaTheme
import com.example.ava.ui.AvaAppRoot
import com.example.ava.ui.settings.AppStateViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appStateViewModel: AppStateViewModel by viewModels()

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

            // Layout direction follows the chosen language, so switching to Persian mirrors
            // every screen without a single manual `if (isRtl)` anywhere in the UI code.
            val direction = if (settings.language == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr

            AvaTheme(themeMode = settings.themeMode, fontScale = settings.fontScale) {
                CompositionLocalProvider(LocalLayoutDirection provides direction) {
                    AvaAppRoot(isLoggedIn = loggedIn)
                }
            }
        }
    }
}
