package com.example.ava.presentation.ui.screens.home

import com.example.ava.domain.models.Song
import com.example.ava.domain.models.Playlist

data class HomeUiState(
    val isLoading: Boolean = true,
    val carouselSongs: List<Song> = emptyList(),
    val trendingSongs: List<Song> = emptyList(),
    val recentSongs: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val error: String? = null
)

sealed class HomeEvent {
    data object OnRefresh : HomeEvent()
    data class OnSongClick(val songId: String) : HomeEvent()
    data class OnPlaylistClick(val playlistId: String) : HomeEvent()
    data class OnQuickAction(val action: QuickAction) : HomeEvent()
}

enum class QuickAction {
    LIKED, RECENT, MY_PLAYLISTS, TOP_ARTISTS
}
