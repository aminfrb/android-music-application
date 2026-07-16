package com.example.ava.service

import android.content.Context
import android.media.AudioAttributes
import android.media.browse.MediaBrowser
import androidx.core.app.NotificationManagerCompat
import com.example.ava.domain.models.Song
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.getValue

class MusicPlayerManager constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: MusicNotificationManager
) {
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position

    private val player: ExoPlayer by lazy {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                _duration.value = duration
                                notificationManager.showNotification(currentSong.value)
                            }
                            Player.STATE_ENDED -> {
                                _isPlaying.value = false
                                notificationManager.hideNotification()
                            }
                        }
                    }
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                        notificationManager.updateNotification(isPlaying)
                    }
                    override fun onPlayerError(error: PlaybackException) {
                        // handle error
                    }
                })
            }
    }

    fun playSong(song: Song, isLocal: Boolean = false) {
        _currentSong.value = song
        val mediaSource = if (isLocal && song.localFilePath != null) {
            // build local source
            ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory())
                .createMediaSource(MediaBrowser.MediaItem.fromUri(song.localFilePath))
        } else {
            // cache-aware source for streaming
            val cache = SimpleCache(context.cacheDir, 100 * 1024 * 1024) // 100 MB
            val dataSourceFactory = CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaBrowser.MediaItem.fromUri(song.audioUrl))
        }
        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = true
    }

    fun playPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun stop() {
        player.stop()
        _isPlaying.value = false
        notificationManager.hideNotification()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun setSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    fun release() {
        player.release()
        notificationManager.hideNotification()
    }
}
