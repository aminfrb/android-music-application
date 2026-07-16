package com.example.ava.presentation

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ava.presentation.screens.downloads.DownloadsScreen
import com.example.ava.presentation.screens.home.HomeScreen
import com.example.ava.presentation.screens.playlists.PlaylistsScreen
import com.example.ava.presentation.screens.profile.ProfileScreen
import com.example.ava.presentation.screens.search.SearchScreen
import com.example.ava.presentation.ui.components.BottomNavigationBar
import com.example.ava.presentation.ui.components.MiniPlayer
import com.example.ava.presentation.ui.theme.AvaTheme
import com.example.ava.utils.PreferenceManager
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class MainActivity : ComponentActivity() {

    private val preferenceManager: PreferenceManager by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applySavedLanguage()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val darkMode by preferenceManager.darkModeFlow.collectAsStateWithLifecycle(initialValue = false)
            val language by preferenceManager.languageFlow.collectAsStateWithLifecycle(initialValue = "en")

            AvaTheme(
                darkTheme = darkMode
            ) {
                MainScreen(
                    onLanguageChange = { newLang ->
                        preferenceManager.setLanguage(newLang)
                        recreate()
                    }
                )
            }
        }
    }

    private fun applySavedLanguage() {
        val lang = preferenceManager.getLanguageSync() ?: "en"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    onLanguageChange: (String) -> Unit
) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val screens = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Downloads,
        Screen.Playlists,
        Screen.Profile
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(modifier = Modifier.fillMaxSize()) {

                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            onSongClick = { songId ->
                                navController.navigate(Screen.Player.createRoute(songId))
                            },
                            onPlaylistClick = { playlistId ->
                            },
                            onQuickAction = { action ->
                            }
                        )
                    }

                    composable(Screen.Search.route) {
                        SearchScreen(
                            onSongClick = { songId ->
                                navController.navigate(Screen.Player.createRoute(songId))
                            }
                        )
                    }

                    composable(Screen.Downloads.route) {
                        DownloadsScreen(
                            onSongClick = { songId ->
                                navController.navigate(Screen.Player.createRoute(songId))
                            }
                        )
                    }

                    composable(Screen.Playlists.route) {
                        PlaylistsScreen(
                            onPlaylistClick = { playlistId ->
                                // navigation to detail
                            }
                        )
                    }

                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            onSettingsClick = {
                                // ناوبری به صفحه تنظیمات
                                navController.navigate(Screen.Settings.route)
                            },
                            onUpgradeClick = {
                                // ارتقا به پریمیوم
                            }
                        )
                    }

                    composable(Screen.Settings.route) {
                        // SettingsScreen(onLanguageChange = onLanguageChange)
                    }

                    composable(
                        route = Screen.Player.route,
                        arguments = Screen.Player.arguments
                    ) { backStackEntry ->
                        val songId = backStackEntry.arguments?.getString("songId") ?: ""
                        // PlayerScreen(songId = songId, onBack = { navController.popBackStack() })
                    }

                    composable(Screen.Chat.route) { /* ChatScreen */ }
                }

                BottomNavigationBar(
                    screens = screens,
                    currentDestination = currentDestination,
                    onTabSelected = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            MiniPlayer(
                onClick = { currentSong ->
                    if (currentSong != null) {
                        // با کلیک روی مینی پلیر به صفحه پلیر کامل می‌رویم
                        navController.navigate(Screen.Player.createRoute(currentSong.id))
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Downloads : Screen("downloads")
    object Playlists : Screen("playlists")
    object Profile : Screen("profile")
    object Settings : Screen("settings")

    object Player : Screen("player/{songId}") {
        val arguments = listOf(
            androidx.navigation.NavArgument("songId") { defaultValue = "" }
        )
        fun createRoute(songId: String) = "player/$songId"
    }

    object Chat : Screen("chat/{userId}") {
        fun createRoute(userId: String) = "chat/$userId"
    }

    companion object {
        // برای راحتی در Bottom Navigation
        fun fromRoute(route: String?): Screen? {
            return when (route?.substringBefore("/")) {
                Home.route -> Home
                Search.route -> Search
                Downloads.route -> Downloads
                Playlists.route -> Playlists
                Profile.route -> Profile
                else -> null
            }
        }
    }
}
