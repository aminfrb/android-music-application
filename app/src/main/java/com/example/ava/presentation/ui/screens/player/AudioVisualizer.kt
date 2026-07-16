package com.example.ava.presentation.ui.screens.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val amplitude by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = repeatable(Int.MAX_VALUE, tween(500))
    )
    Canvas(modifier = modifier.size(200.dp, 100.dp)) {
        val step = size.width / 30
        for (i in 0 until 30) {
            val height = (amplitude * (20 + Random.nextInt(60))) * (size.height / 100)
            drawRect(
                color = Color.White.copy(alpha = 0.7f),
                topLeft = Offset(i * step, size.height / 2 - height / 2),
                size = Size(step * 0.5f, height)
            )
        }
    }
}