package com.example.ava.presentation.ui.screens.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val song = uiState.song
    val palette = rememberPalette(song?.coverImageUrl)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette?.vibrant?.toComposeColor() ?: Color.Black)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            // Shared element transition for cover
            AnimatedContent(
                targetState = song,
                transitionSpec = {
                    fadeIn() + scaleIn() with fadeOut() + scaleOut()
                }
            ) { currentSong ->
                if (currentSong != null) {
                    AsyncImage(
                        model = currentSong.coverImageUrl,
                        contentDescription = "Cover",
                        modifier = Modifier
                            .size(300.dp)
                            .rotate(if (uiState.isPlaying) 360f else 0f)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = song?.title ?: "", style = MaterialTheme.typography.headlineSmall)
            Text(text = song?.artistName ?: "", style = MaterialTheme.typography.bodyMedium)

            // Custom Audio Visualizer (Canvas)
            AudioVisualizer(isPlaying = uiState.isPlaying)

            // SeekBar, Play/Pause, Next, Prev, Speed controls
            // ...
        }
    }
}
