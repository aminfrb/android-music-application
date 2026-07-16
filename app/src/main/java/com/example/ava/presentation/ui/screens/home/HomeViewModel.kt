package com.example.ava.presentation.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ava.domain.repository.PlaylistRepository
import com.example.ava.domain.repository.SongRepository
import com.example.ava.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val songRepo: SongRepository,
    private val playlistRepo: PlaylistRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnRefresh -> loadHomeData()
            is HomeEvent.OnSongClick -> handleSongClick(event.songId)
            is HomeEvent.OnPlaylistClick -> handlePlaylistClick(event.playlistId)
            is HomeEvent.OnQuickAction -> handleQuickAction(event.action)
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val trending = songRepo.getTrendingSongs(10)
                val carousel = songRepo.getTrendingSongs(5) // or separate API
                val playlists = playlistRepo.getFeaturedPlaylists()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        carouselSongs = carousel.getOrElse { emptyList() },
                        trendingSongs = trending.getOrElse { emptyList() },
                        playlists = playlists.getOrElse { emptyList() }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun handleSongClick(songId: String) {
        // navigate to player
    }

    private fun handlePlaylistClick(playlistId: String) {
        // navigate to playlist detail
    }

    private fun handleQuickAction(action: QuickAction) {
        // handle navigation or data fetch
    }
}
