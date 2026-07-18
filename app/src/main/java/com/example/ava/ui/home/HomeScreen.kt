package com.example.ava.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ava.core.designsystem.component.AvaTopBar
import com.example.ava.core.designsystem.component.EmptyState
import com.example.ava.core.designsystem.component.ShimmerBox
import com.example.ava.core.designsystem.component.ShimmerCardRow
import com.example.ava.core.designsystem.component.pressScale
import com.example.ava.core.designsystem.theme.AvaTheme
import com.example.ava.domain.model.Playlist
import com.example.ava.domain.model.Song
import com.example.ava.ui.navigation.Destination
import kotlinx.coroutines.delay
import com.example.ava.R

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val offlineMessage = stringResource(R.string.error_offline_title)

    LaunchedEffect(Unit) {
        viewModel.effects.collect {
            when (it) {
                HomeEffect.ShowOfflineSnackbar -> snackbarHostState.showSnackbar(offlineMessage)
            }
        }
    }

    Scaffold(
        topBar = {
            AvaTopBar(
                avatarUrl = state.avatarUrl,
                onProfileClick = { navController.navigate(Destination.Profile.route) },
                onNotificationsClick = { navController.navigate(Destination.Conversations.route) },
                onSettingsClick = { navController.navigate(Destination.Settings.route) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.hasError && state.carousel.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.error_offline_title),
                body = stringResource(R.string.error_offline_body),
                actionLabel = stringResource(R.string.retry),
                onAction = { viewModel.onEvent(HomeEvent.Refresh) },
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = AvaTheme.sizes.miniPlayerHeight + AvaTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(AvaTheme.spacing.lg),
        ) {
            item {
                if (state.isLoading) {
                    ShimmerBox(
                        Modifier
                            .padding(horizontal = AvaTheme.spacing.screen)
                            .fillMaxWidth()
                            .height(AvaTheme.sizes.carouselHeight),
                        MaterialTheme.shapes.large,
                    )
                } else {
                    DailyPicksCarousel(
                        songs = state.carousel,
                        onSongClick = { viewModel.onEvent(HomeEvent.PlaySong(it, state.carousel)) },
                    )
                }
            }

            item { QuickActions(navController) }

            item {
                SectionHeader(stringResource(R.string.home_most_popular))
                if (state.isLoading) ShimmerCardRow()
                else SongRail(state.popular) { viewModel.onEvent(HomeEvent.PlaySong(it, state.popular)) }
            }

            item {
                SectionHeader(stringResource(R.string.home_newest))
                if (state.isLoading) ShimmerCardRow()
                else SongRail(state.newest) { viewModel.onEvent(HomeEvent.PlaySong(it, state.newest)) }
            }

            item {
                SectionHeader(stringResource(R.string.home_world_playlists))
                if (state.isLoading) ShimmerCardRow()
                else PlaylistRail(state.worldPlaylists) {
                    navController.navigate(Destination.PlaylistDetail.of(it.id))
                }
            }

            item {
                SectionHeader(stringResource(R.string.home_local_playlists))
                if (state.isLoading) ShimmerCardRow()
                else PlaylistRail(state.localPlaylists) {
                    navController.navigate(Destination.PlaylistDetail.of(it.id))
                }
            }
        }
    }
}

/** Auto-advancing hero carousel. Pauses nothing, loops forever, no dots — the art is the point. */
@Composable
private fun DailyPicksCarousel(songs: List<Song>, onSongClick: (Song) -> Unit) {
    if (songs.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { songs.size })

    LaunchedEffect(songs.size) {
        while (true) {
            delay(4_500)
            val next = (pagerState.currentPage + 1) % songs.size
            pagerState.animateScrollToPage(next)
        }
    }

    Column {
        SectionHeader(stringResource(R.string.home_picks_today))
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = AvaTheme.spacing.screen),
            pageSpacing = AvaTheme.spacing.gutter,
        ) { page ->
            val song = songs[page]
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(AvaTheme.sizes.carouselHeight)
                    .clip(MaterialTheme.shapes.large)
                    .pressScale(pressedScale = 0.98f) { onSongClick(song) },
            ) {
                AsyncImage(
                    model = song.coverImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.5f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.75f),
                            )
                        )
                )
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(AvaTheme.spacing.md)
                ) {
                    Text(
                        song.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        song.artistName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActions(navController: NavController) {
    data class Action(val labelRes: Int, val icon: ImageVector, val route: String)
    val actions = listOf(
        Action(R.string.home_liked_songs, Icons.Filled.Favorite, Destination.LikedSongs.route),
        Action(R.string.home_recently_played, Icons.Filled.History, Destination.RecentlyPlayed.route),
        Action(R.string.home_my_playlists, Icons.Filled.QueueMusic, Destination.Playlists.route),
        Action(R.string.home_top_artists, Icons.Filled.Star, Destination.TopArtists.route),
    )

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = AvaTheme.spacing.screen),
        horizontalArrangement = Arrangement.spacedBy(AvaTheme.spacing.sm),
    ) {
        actions.forEach { action ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .pressScale { navController.navigate(action.route) },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Column(
                    Modifier.padding(vertical = AvaTheme.spacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AvaTheme.spacing.xs),
                ) {
                    Icon(action.icon, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(
                        stringResource(action.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier.padding(
            horizontal = AvaTheme.spacing.screen,
            vertical = AvaTheme.spacing.sm,
        ),
    )
}

@Composable
private fun SongRail(songs: List<Song>, onClick: (Song) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = AvaTheme.spacing.screen),
        horizontalArrangement = Arrangement.spacedBy(AvaTheme.spacing.gutter),
    ) {
        items(songs, key = { it.id }) { song ->
            Column(
                Modifier
                    .width(AvaTheme.sizes.artCard)
                    .pressScale { onClick(song) }
            ) {
                AsyncImage(
                    model = song.coverImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(AvaTheme.sizes.artCard)
                        .clip(MaterialTheme.shapes.medium),
                )
                Spacer(Modifier.height(AvaTheme.spacing.sm))
                Text(song.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    song.artistName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PlaylistRail(playlists: List<Playlist>, onClick: (Playlist) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = AvaTheme.spacing.screen),
        horizontalArrangement = Arrangement.spacedBy(AvaTheme.spacing.gutter),
    ) {
        items(playlists, key = { it.id }) { playlist ->
            Column(
                Modifier
                    .width(AvaTheme.sizes.artCard)
                    .pressScale { onClick(playlist) }
            ) {
                AsyncImage(
                    model = playlist.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(AvaTheme.sizes.artCard)
                        .clip(MaterialTheme.shapes.medium),
                )
                Spacer(Modifier.height(AvaTheme.spacing.sm))
                Text(playlist.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    stringResource(R.string.playlist_song_count, playlist.songCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
