package com.example.ava.ui.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ava.core.designsystem.component.EmptyState
import com.example.ava.core.designsystem.component.SongRow
import com.example.ava.core.designsystem.theme.AvaTheme
import com.example.ava.domain.repository.DownloadSort
import com.example.ava.R

/** Swipe a row to the end of the line to remove the file. The red panel appears as you drag. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(viewModel: DownloadsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var sortMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.downloads_title)) },
                actions = {
                    IconButton(onClick = { sortMenuOpen = true }) {
                        Icon(Icons.Filled.Sort, stringResource(R.string.downloads_sort_date))
                    }
                    DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                        listOf(
                            DownloadSort.DATE to R.string.downloads_sort_date,
                            DownloadSort.TITLE to R.string.downloads_sort_title,
                            DownloadSort.ARTIST to R.string.downloads_sort_artist,
                        ).forEach { (sort, label) ->
                            DropdownMenuItem(
                                text = { Text(stringResource(label)) },
                                onClick = {
                                    viewModel.onEvent(DownloadsEvent.SortChanged(sort))
                                    sortMenuOpen = false
                                },
                            )
                        }
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
            )
        }
    ) { padding ->
        if (state.songs.isEmpty() && !state.isLoading) {
            EmptyState(
                title = stringResource(R.string.empty_downloads_title),
                body = stringResource(R.string.empty_downloads_body),
                modifier = Modifier.padding(padding),
            )
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
                    Button(
                        onClick = { viewModel.onEvent(DownloadsEvent.PlayAll) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Filled.PlayArrow, null)
                        Spacer(Modifier.width(AvaTheme.spacing.xs))
                        Text(stringResource(R.string.play_all))
                    }
                    OutlinedButton(
                        onClick = { viewModel.onEvent(DownloadsEvent.ShuffleAll) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Filled.Shuffle, null)
                        Spacer(Modifier.width(AvaTheme.spacing.xs))
                        Text(stringResource(R.string.shuffle_all))
                    }
                }
            }

            items(state.songs, key = { it.id }) { song ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            viewModel.onEvent(DownloadsEvent.Delete(song.id))
                            true
                        } else false
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
                        SongRow(song = song, onClick = { viewModel.onEvent(DownloadsEvent.Play(song)) })
                    }
                }
            }
        }
    }
}
