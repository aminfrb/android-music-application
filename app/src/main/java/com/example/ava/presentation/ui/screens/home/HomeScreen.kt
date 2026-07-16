package com.example.ava.presentation.ui.screens.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onSongClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onQuickAction: (QuickAction) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { /* CustomTopBar with logo, avatar, bell, gear */ }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                if (uiState.isLoading) {
                    ShimmerEffect(width = 300.dp, height = 200.dp)
                } else {
                    CarouselSlider(songs = uiState.carouselSongs, onSongClick)
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Quick Actions Row
                // ...
                // Trending Row
                SongRow(title = "Trending", songs = uiState.trendingSongs, onSongClick)
                // Playlists Row
                // ...
            }
        }
    }
}

