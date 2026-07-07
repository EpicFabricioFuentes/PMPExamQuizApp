package com.fax.passyourpmpexam.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fax.passyourpmpexam.core.designsystem.component.PrimaryButton
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing
import com.fax.passyourpmpexam.core.designsystem.theme.PmpTheme

/**
 * One-time first-run welcome. A stateless intro screen with a top-weighted header, three
 * feature cards, and a bottom-anchored primary action (per SSOT); [onGetStarted] persists the
 * first-run flag and drops the user into the app. The cards are informational only.
 */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = PmpSpacing.safeMargin),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(PmpSpacing.safeMargin),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit * 2)) {
                    Text(
                        text = "Master the PMP",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Build exam readiness a little every day — no account needed, fully offline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(PmpSpacing.gridUnit * 2))
                OnboardingCard(
                    icon = Icons.Filled.CalendarToday,
                    title = "Daily Question",
                    description = "One fresh question a day keeps your streak alive.",
                )
                OnboardingCard(
                    icon = Icons.Filled.Timer,
                    title = "Quiz Mode",
                    description = "Short sets or a full 180-question mock with per-domain scoring.",
                )
                OnboardingCard(
                    icon = Icons.Filled.FilterList,
                    title = "Free Mode",
                    description = "Unlimited questions, filterable by domain, with instant explanations.",
                )
            }
            PrimaryButton(
                text = "Get Started",
                onClick = onGetStarted,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = PmpSpacing.itemGap, bottom = PmpSpacing.safeMargin),
            )
        }
    }
}

/** An informational feature card: a tinted circular icon beside a title and description. */
@Composable
private fun OnboardingCard(
    icon: ImageVector,
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = MaterialTheme.shapes.extraLarge,
            )
            .padding(PmpSpacing.basePadding),
        horizontalArrangement = Arrangement.spacedBy(PmpSpacing.safeMargin),
        verticalAlignment = Alignment.Top,
    ) {
        OnboardingIcon(icon)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OnboardingIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Preview(name = "Welcome")
@Composable
private fun WelcomeScreenPreview() {
    PmpTheme {
        WelcomeScreen(onGetStarted = {})
    }
}
