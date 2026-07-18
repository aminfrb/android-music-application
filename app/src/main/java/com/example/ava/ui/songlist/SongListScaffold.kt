package com.example.ava.ui.songlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.ava.core.designsystem.component.EmptyState
import com.example.ava.core.designsystem.component.SongRow
import com.example.ava.core.designsystem.theme.AvaTheme
import com.example.ava.domain.model.Song
import com.example.ava.R

/**
 * "Liked", "Recently played" and any future song list are the same screen with different
 * data: a title, play-all / shuffle, and optional swipe-to-remove. Writing it once means
 * the three of them can never drift apart.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListScaffold(
    title: String,
    songs: List<Song>,
    navController: NavController,
    emptyTitle: String,
    emptyBody: String,
    onPlay: (Int) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onRemove: ((Song) -> Unit)? = null,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back))
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
            )
        }
    ) { padding ->
        if (songs.isEmpty()) {
            EmptyState(emptyTitle, emptyBody, Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = AvaTheme.sizes.miniPlayerHeight + AvaTheme.spacing.xl),
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(AvaTheme.spacing.screen),
                    horizontalArrangement = Arrangement.spacedBy(AvaTheme.spacing.sm),
                ) {
                    Button(onClick = onPlayAll, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.PlayArrow, null)
                        Spacer(Modifier.width(AvaTheme.spacing.xs))
                        Text(stringResource(R.string.play_all))
                    }
                    OutlinedButton(onClick = onShuffleAll, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Shuffle, null)
                        Spacer(Modifier.width(AvaTheme.spacing.xs))
                        Text(stringResource(R.string.shuffle_all))
                    }
                }
            }

            itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                if (onRemove == null) {
                    SongRow(song = song, onClick = { onPlay(index) })
                } else {
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) { onRemove(song); true } else false
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.error)
                                    .padding(horizontal = AvaTheme.spacing.lg),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    stringResource(R.string.cd_remove),
                                    tint = MaterialTheme.colorScheme.onError,
                                )
                            }
                        },
                    ) {
                        Surface(color = MaterialTheme.colorScheme.background) {
                            SongRow(song = song, onClick = { onPlay(index) })
                        }
                    }
                }
            }
        }
    }
}
