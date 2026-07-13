package com.fax.passyourpmpexam.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fax.passyourpmpexam.core.designsystem.theme.PmpCorrect
import com.fax.passyourpmpexam.core.designsystem.theme.PmpCorrectContainer
import com.fax.passyourpmpexam.core.designsystem.theme.PmpIncorrect
import com.fax.passyourpmpexam.core.designsystem.theme.PmpIncorrectContainer
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing
import com.fax.passyourpmpexam.core.designsystem.theme.PmpTheme

/**
 * Visual state of a single answer option:
 * - [DEFAULT] unanswered; [SELECTED] chosen but not yet graded (quiz mode);
 * - [CORRECT]/[INCORRECT] post-grading feedback (green/red are reserved for exactly this per the SSOT).
 */
enum class OptionState { DEFAULT, SELECTED, CORRECT, INCORRECT }

/**
 * A tappable answer option row. Stateless — the caller supplies the [state]. [label] is the option
 * letter (A/B/C/D) shown in the leading badge; on grading the badge becomes a check (correct) or a
 * cross (incorrect). Untouched options dim once the row is no longer [enabled] (i.e. after grading).
 */
@Composable
fun AnswerOption(
    text: String,
    label: String,
    state: OptionState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = optionColors(state)
    // Neutral rows fade back once grading disables the row, keeping focus on the graded options.
    val dim = state == OptionState.DEFAULT && !enabled
    // Subtle pop-in on the correct answer, mirroring the mockup's ripple.
    val scale by animateFloatAsState(
        targetValue = if (state == OptionState.CORRECT) 1f else 0.98f,
        label = "correctScale",
    )
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = colors.container,
        contentColor = colors.content,
        border = colors.border,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = PmpSpacing.touchTargetMin)
            .then(if (state == OptionState.CORRECT) Modifier.scale(scale) else Modifier)
            .alpha(if (dim) 0.6f else 1f),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            OptionBadge(label = label, state = state)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = PmpSpacing.itemGap),
            )
        }
    }
}

/** Leading circular badge: option letter by default, a check/cross once graded. */
@Composable
private fun OptionBadge(label: String, state: OptionState) {
    val scheme = MaterialTheme.colorScheme
    when (state) {
        OptionState.CORRECT -> FilledBadge(background = PmpCorrect) {
            // The badge conveys correctness visually; label it so TalkBack announces the outcome.
            Icon(Icons.Filled.Check, contentDescription = "Correct answer", tint = Color.White, modifier = Modifier.size(16.dp))
        }
        OptionState.INCORRECT -> FilledBadge(background = PmpIncorrect) {
            Icon(Icons.Filled.Close, contentDescription = "Incorrect answer", tint = Color.White, modifier = Modifier.size(16.dp))
        }
        OptionState.SELECTED -> LetterBadge(label = label, border = scheme.primary, textColor = scheme.primary)
        OptionState.DEFAULT -> LetterBadge(label = label, border = scheme.outlineVariant, textColor = scheme.onSurfaceVariant)
    }
}

@Composable
private fun FilledBadge(background: Color, content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(background),
    ) { content() }
}

@Composable
private fun LetterBadge(label: String, border: Color, textColor: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .border(2.dp, border, CircleShape),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = textColor)
    }
}

private data class OptionColors(
    val container: Color,
    val content: Color,
    val border: BorderStroke,
)

@Composable
private fun optionColors(state: OptionState): OptionColors {
    val scheme = MaterialTheme.colorScheme
    return when (state) {
        OptionState.DEFAULT ->
            OptionColors(scheme.surface, scheme.onSurface, BorderStroke(1.dp, scheme.outlineVariant))
        OptionState.SELECTED ->
            OptionColors(scheme.primaryContainer, scheme.onPrimaryContainer, BorderStroke(1.dp, scheme.primary))
        OptionState.CORRECT ->
            OptionColors(PmpCorrectContainer, PmpCorrect, BorderStroke(2.dp, PmpCorrect))
        OptionState.INCORRECT ->
            OptionColors(PmpIncorrectContainer, PmpIncorrect, BorderStroke(2.dp, PmpIncorrect))
    }
}

@Preview(name = "AnswerOption states")
@Composable
private fun AnswerOptionStatesPreview() {
    PmpTheme {
        Column(
            modifier = Modifier.padding(PmpSpacing.safeMargin),
            verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
        ) {
            // Pre-grading: tappable letter badges.
            AnswerOption("Initiating", "A", OptionState.DEFAULT, enabled = true, onClick = {})
            AnswerOption("Planning", "B", OptionState.SELECTED, enabled = true, onClick = {})
            // Post-grading: check/cross badges, and a dimmed untouched row.
            AnswerOption("Closing", "C", OptionState.CORRECT, enabled = false, onClick = {})
            AnswerOption("Executing", "D", OptionState.INCORRECT, enabled = false, onClick = {})
            AnswerOption("Monitoring", "E", OptionState.DEFAULT, enabled = false, onClick = {})
        }
    }
}
