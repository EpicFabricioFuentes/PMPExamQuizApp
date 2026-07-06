package com.fax.passyourpmpexam.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fax.passyourpmpexam.core.designsystem.component.PrimaryButton
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing

/**
 * One-time first-run welcome. A stateless intro screen with a bottom-anchored primary action
 * (per SSOT); [onGetStarted] persists the first-run flag and drops the user into the app.
 */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PmpSpacing.basePadding),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Pass Your PMP Exam",
                style = MaterialTheme.typography.displayLarge,
            )
            Text(
                text = "Build exam readiness a little every day — no account needed, fully offline.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.weight(1f))
            WelcomeHighlights()
            Spacer(modifier = Modifier.weight(2f))
        }
        PrimaryButton(
            text = "Get started",
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PmpSpacing.itemGap),
        )
    }
}

@Composable
private fun WelcomeHighlights(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit),
    ) {
        Highlight("Daily Question", "One fresh question a day keeps your streak alive.")
        Highlight("Timed quizzes", "Short sets or a full 180-question mock with per-domain scoring.")
        Highlight("Free practice", "Unlimited questions, filterable by domain, with instant explanations.")
    }
}

@Composable
private fun Highlight(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit)) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
