package com.example.ava.core.designsystem.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ava.core.designsystem.theme.AvaTheme
import kotlin.math.sin

/**
 * Empty screens are an invitation, not an apology: a flat, breathing waveform, a plain
 * statement of what's missing, and one concrete next step. Drawn with Canvas — no Lottie.
 */
@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val transition = rememberInfiniteTransition(label = "emptyWave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing)),
        label = "phase",
    )
    val stroke = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AvaTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Canvas(Modifier.fillMaxWidth(0.6f).height(72.dp)) {
            val steps = 64
            val amp = size.height / 3f
            var prev = Offset(0f, size.height / 2f)
            repeat(steps) { i ->
                val t = i / steps.toFloat()
                val damp = 0.35f + 0.65f * sin(t * Math.PI).toFloat()
                val y = size.height / 2f + sin(t * 10f + phase) * amp * damp
                val point = Offset(t * size.width, y)
                drawLine(stroke, prev, point, strokeWidth = 3f)
                prev = point
            }
        }
        Spacer(Modifier.height(AvaTheme.spacing.lg))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(AvaTheme.spacing.sm))
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(AvaTheme.spacing.lg))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}
