package com.example.ava.domain.repository

import com.example.ava.domain.models.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    suspend fun getTrendingSongs(limit: Int = 20): Result<List<Song>>
    suspend fun searchSongs(query: String): Flow<Result<List<Song>>>
    suspend fun getLikedSongs(): Flow<List<Song>>
    suspend fun toggleLike(songId: String): Result<Unit>
    suspend fun downloadSong(songId: String): Result<String> // returns local path
    suspend fun getDownloadedSongs(): Flow<List<Song>>
    suspend fun playSong(songId: String): Result<Unit>
}