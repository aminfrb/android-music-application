package com.example.ava.domain.repository

import com.example.ava.data.local.dao.SongDao
import com.example.ava.data.local.entities.SongEntity
import com.example.ava.data.remote.api.MusicApiService
import com.example.ava.data.remote.models.SongDto
import com.example.ava.data.repository.mapper.toDomain
import com.example.ava.data.repository.mapper.toEntity
import com.example.ava.domain.models.Song
import com.example.ava.domain.repository.SongRepository
import com.example.ava.domain.utils.Result
import com.example.ava.utils.PreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Singleton

@Singleton
class SongRepositoryImpl(
    private val api: MusicApiService,
    private val songDao: SongDao,
    private val preferenceManager: PreferenceManager
) : SongRepository {

    override suspend fun getTrendingSongs(limit: Int): Result<List<Song>> {
        return try {
            val response = api.getTrendingSongs(limit)
            if (response.isSuccessful) {
                val songs = response.body()?.songs?.map { it.toDomain() } ?: emptyList()
                // Cache in DB (optional)
                Result.Success(songs)
            } else {
                Result.Error(Exception("Server error"))
            }
        } catch (e: Exception) {
            // fallback to local cache
            val cached = songDao.getAllSongs().map { it.toDomain() }
            if (cached.isNotEmpty()) Result.Success(cached)
            else Result.Error(e)
        }
    }

    override suspend fun searchSongs(query: String): Flow<Result<List<Song>>> = flow {
        try {
            val response = api.searchSongs(query)
            if (response.isSuccessful) {
                val songs = response.body()?.songs?.map { it.toDomain() } ?: emptyList()
                emit(Result.Success(songs))
                // Save search history
                // ...
            } else {
                emit(Result.Error(Exception("Search failed")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    override fun getLikedSongs(): Flow<List<Song>> =
        songDao.getLikedSongs().map { entities -> entities.map { it.toDomain() } }

    override suspend fun toggleLike(songId: String): Result<Unit> = runCatching {
        songDao.toggleLike(songId)
        Result.Success(Unit)
    }.getOrElse { Result.Error(it) }

    override suspend fun downloadSong(songId: String): Result<String> {
        // Check premium
        if (!preferenceManager.isPremium()) {
            return Result.Error(Exception("Upgrade to premium to download"))
        }
        // Enqueue WorkManager
        // ...
        return Result.Success("/path/to/song.mp3")
    }

    override fun getDownloadedSongs(): Flow<List<Song>> =
        songDao.getDownloadedSongs().map { entities -> entities.map { it.toDomain() } }

    override suspend fun playSong(songId: String): Result<Unit> {
        // Check if local file exists -> play local, else stream
        // ...
        return Result.Success(Unit)
    }
}