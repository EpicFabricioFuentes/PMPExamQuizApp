package com.fax.passyourpmpexam.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fax.passyourpmpexam.core.designsystem.theme.PmpTheme

/**
 * A large circular progress ring with a centered [content] slot — used for the quiz-results hero
 * score. Draws a full-circle track plus a rounded progress arc that sweeps clockwise from 12 o'clock,
 * animating whenever [progress] (0f..1f) changes.
 */
@Composable
fun CircularScoreRing(
    progress: Float,
    ringColor: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    diameter: Int = 180,
    strokeWidth: Int = 12,
    content: @Composable () -> Unit,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "scoreRing",
    )
    Box(contentAlignment = Alignment.Center, modifier = modifier.size(diameter.dp)) {
        Canvas(modifier = Modifier.size(diameter.dp)) {
            val stroke = Stroke(width = strokeWidth.dp.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width / 2f
            val arcSize = androidx.compose.ui.geometry.Size(
                width = size.width - stroke.width,
                height = size.height - stroke.width,
            )
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            // Track (full circle).
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
            // Progress arc, clockwise from the top.
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = animated * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }
        content()
    }
}

@Preview
@Composable
private fun CircularScoreRingPreview() {
    PmpTheme {
        CircularScoreRing(progress = 0.78f, ringColor = MaterialTheme.colorScheme.secondary) {
            Text("78%", style = MaterialTheme.typography.displayLarge)
        }
    }
}
