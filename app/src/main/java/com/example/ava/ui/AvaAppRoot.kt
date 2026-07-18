package com.example.ava.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.ava.ui.auth.AuthScreen
import com.example.ava.ui.chat.ChatScreen
import com.example.ava.ui.chat.ConversationsScreen
import com.example.ava.ui.downloads.DownloadsScreen
import com.example.ava.ui.home.HomeScreen
import com.example.ava.ui.navigation.Destination
import com.example.ava.ui.navigation.AvaBottomBar
import com.example.ava.ui.navigation.bottomTabs
import com.example.ava.ui.player.MiniPlayer
import com.example.ava.ui.player.NowPlayingScreen
import com.example.ava.ui.player.PlayerEvent
import com.example.ava.ui.player.PlayerViewModel
import com.example.ava.ui.playlists.PlaylistDetailScreen
import com.example.ava.ui.playlists.PlaylistsScreen
import com.example.ava.ui.profile.ProfileScreen
import com.example.ava.ui.search.SearchScreen
import com.example.ava.ui.settings.SettingsScreen
import com.example.ava.ui.social.FindFriendsScreen
import com.example.ava.ui.social.FollowingScreen
import com.example.ava.ui.social.UserProfileScreen
import com.example.ava.ui.songlist.LikedSongsScreen
import com.example.ava.ui.songlist.RecentlyPlayedScreen
import com.example.ava.ui.songlist.TopArtistsScreen

/**
 * The whole app sits inside one SharedTransitionLayout, which is what lets the mini player's
 * cover morph into the disc on the Now Playing screen instead of cross-fading.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AvaAppRoot(isLoggedIn: Boolean) {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val playback by playerViewModel.playback.collectAsStateWithLifecycle()

    var playerExpanded by remember { mutableStateOf(false) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomTabs.any { it.destination.route == currentRoute }

    if (!isLoggedIn) {
        AuthScreen()
        return
    }

    SharedTransitionLayout {
        AnimatedContent(
            targetState = playerExpanded,
            transitionSpec = {
                (fadeIn(tween(300)) togetherWith fadeOut(tween(300)))
            },
            label = "playerExpansion",
        ) { expanded ->
            if (expanded) {
                NowPlayingScreen(
                    animatedVisibilityScope = this@AnimatedContent,
                    onCollapse = { playerExpanded = false },
                    onShare = { songId -> navController.navigate(Destination.Conversations.route) },
                    viewModel = playerViewModel,
                )
            } else {
                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            AvaBottomBar(
                                currentRoute = currentRoute
                            ) { destination ->
                                navController.navigate(destination.route) {
                                    popUpTo(Destination.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    },
                    floatingActionButton = {},
                ) { padding ->
                    Box(Modifier.fillMaxSize()) {
                        AvaNavHost(
                            navController = navController,
                            modifier = Modifier.padding(padding),
                        )
                        if (showBottomBar) {
                            playback.currentSong?.let { song ->
                                MiniPlayer(
                                    song = song,
                                    isPlaying = playback.isPlaying,
                                    progress = if (playback.durationMs > 0)
                                        playback.positionMs.toFloat() / playback.durationMs else 0f,
                                    animatedVisibilityScope = this@AnimatedContent,
                                    onExpand = { playerExpanded = true },
                                    onTogglePlay = { playerViewModel.onEvent(PlayerEvent.TogglePlayPause) },
                                    onNext = { playerViewModel.onEvent(PlayerEvent.Next) },
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = padding.calculateBottomPadding() + Dp(4f)),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvaNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Destination.Home.route,
        modifier = modifier,
        // Screens slide in from the trailing edge; Compose mirrors this automatically in RTL.
        enterTransition = { slideInHorizontally(tween(280)) { it / 6 } + fadeIn(tween(280)) },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { slideOutHorizontally(tween(280)) { it / 6 } + fadeOut(tween(180)) },
    ) {
        composable(Destination.Home.route) { HomeScreen(navController) }
        composable(Destination.Search.route) { SearchScreen() }
        composable(Destination.Downloads.route) { DownloadsScreen() }
        composable(Destination.Playlists.route) { PlaylistsScreen(navController) }
        composable(Destination.Profile.route) { ProfileScreen(navController) }

        composable(Destination.Settings.route) { SettingsScreen(navController) }
        composable(Destination.LikedSongs.route) { LikedSongsScreen(navController) }
        composable(Destination.RecentlyPlayed.route) { RecentlyPlayedScreen(navController) }
        composable(Destination.TopArtists.route) { TopArtistsScreen(navController) }
        composable(Destination.Following.route) { FollowingScreen(navController) }
        composable(Destination.Conversations.route) { ConversationsScreen(navController) }
        composable(Destination.FindFriends.route) { FindFriendsScreen(navController) }

        composable(
            Destination.PlaylistDetail.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType }),
        ) { PlaylistDetailScreen(navController) }

        composable(
            Destination.UserProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType }),
        ) { UserProfileScreen(navController) }

        composable(
            Destination.Chat.route,
            arguments = listOf(
                navArgument("conversationId") { type = NavType.LongType },
                navArgument("peerId") { type = NavType.LongType },
            ),
        ) { ChatScreen(navController) }
    }
}
