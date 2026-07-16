package com.example.ava.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.ava.R
import com.example.ava.presentation.MainActivity

class MusicNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "music_channel"
    private val notificationId = 1001

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows currently playing music"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(song: Song?) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = PendingIntent.getBroadcast(
            context, 1, Intent("PLAY_PAUSE"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nextIntent = PendingIntent.getBroadcast(
            context, 2, Intent("NEXT"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(song?.title ?: "Music")
            .setContentText(song?.artistName ?: "")
            .setSmallIcon(R.drawable.ic_music_note)
            .setLargeIcon(/* bitmap from cover */)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(R.drawable.ic_skip_previous, "Previous", null) // optional
            .addAction(R.drawable.ic_play_pause, "Play/Pause", playPauseIntent)
            .addAction(R.drawable.ic_skip_next, "Next", nextIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(1))
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun updateNotification(isPlaying: Boolean) {
        // update with new play/pause icon
    }

    fun hideNotification() {
        notificationManager.cancel(notificationId)
    }
}
