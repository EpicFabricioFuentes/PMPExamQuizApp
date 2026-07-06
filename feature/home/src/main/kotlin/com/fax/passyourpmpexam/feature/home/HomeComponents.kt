package com.fax.passyourpmpexam.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing
import com.fax.passyourpmpexam.core.designsystem.theme.PmpTheme

/** Two-tone "PMP Prep" brand wordmark for the Home top bar. */
@Composable
internal fun Wordmark(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "PMP",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(PmpSpacing.gridUnit))
        Text(
            text = "Prep",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * A compact status tile (streak / daily goal): an icon + label, a headline value, and a thin
 * rounded progress bar. Fixed height so a row of them lines up.
 */
@Composable
internal fun StatusCard(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    progress: Float,
    progressColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(128.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.large,
            )
            .padding(PmpSpacing.safeMargin),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit),
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit)) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
        }
    }
}

/** Colors + copy for a [ModeCard]. Keeps the many surface roles out of the call site. */
internal data class ModeCardStyle(
    val containerColor: Color,
    val contentColor: Color,
    val bodyColor: Color,
    val chipContainer: Color,
    val chipContent: Color,
    val iconTint: Color,
    val iconTileColor: Color,
    val ctaColor: Color,
)

/**
 * A large rounded "learning mode" entry card: a capsule chip, title + body on the left, a rounded
 * icon tile on the right, and a "cta →" row. Colors come from [style].
 */
@Composable
internal fun ModeCard(
    chipText: String,
    title: String,
    body: String,
    icon: ImageVector,
    ctaText: String,
    style: ModeCardStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(style.containerColor)
            .clickable(onClick = onClick)
            .padding(PmpSpacing.basePadding),
        verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit * 2),
            ) {
                Chip(text = chipText, container = style.chipContainer, content = style.chipContent)
                Text(text = title, style = MaterialTheme.typography.headlineMedium, color = style.contentColor)
                Text(text = body, style = MaterialTheme.typography.bodyMedium, color = style.bodyColor)
            }
            Spacer(Modifier.width(PmpSpacing.itemGap))
            IconTile(icon = icon, tint = style.iconTint, background = style.iconTileColor)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit),
        ) {
            Text(text = ctaText, style = MaterialTheme.typography.titleSmall, color = style.ctaColor)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = style.ctaColor,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun Chip(text: String, container: Color, content: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = content,
        modifier = Modifier
            .clip(CircleShape)
            .background(container)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

@Composable
private fun IconTile(icon: ImageVector, tint: Color, background: Color) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
    }
}

@Preview
@Composable
private fun StatusCardPreview() {
    PmpTheme {
        Row(
            modifier = Modifier.padding(PmpSpacing.safeMargin),
            horizontalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
        ) {
            StatusCard(
                icon = Icons.Filled.Whatshot,
                iconTint = MaterialTheme.colorScheme.tertiary,
                label = "Current",
                value = "5 day streak",
                progress = 0.7f,
                progressColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f),
            )
            StatusCard(
                icon = Icons.Filled.Quiz,
                iconTint = MaterialTheme.colorScheme.primary,
                label = "Daily Goal",
                value = "0/1 Done",
                progress = 0f,
                progressColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
private fun ModeCardPreview() {
    PmpTheme {
        val cs = MaterialTheme.colorScheme
        ModeCard(
            chipText = "EASY WIN",
            title = "Daily Question",
            body = "One focused question to keep your momentum alive.",
            icon = Icons.Filled.CalendarToday,
            ctaText = "Start now",
            style = ModeCardStyle(
                containerColor = cs.primaryContainer,
                contentColor = Color.White,
                bodyColor = Color.White.copy(alpha = 0.85f),
                chipContainer = Color.White.copy(alpha = 0.2f),
                chipContent = Color.White,
                iconTint = Color.White,
                iconTileColor = Color.White.copy(alpha = 0.15f),
                ctaColor = Color.White,
            ),
            onClick = {},
            modifier = Modifier.padding(PmpSpacing.safeMargin),
        )
    }
}
