package com.fax.passyourpmpexam.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fax.passyourpmpexam.core.designsystem.theme.PmpCorrect
import com.fax.passyourpmpexam.core.designsystem.theme.PmpCorrectContainer
import com.fax.passyourpmpexam.core.designsystem.theme.PmpIncorrect
import com.fax.passyourpmpexam.core.designsystem.theme.PmpIncorrectContainer
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing

/**
 * Visual state of a single answer option:
 * - [DEFAULT] unanswered; [SELECTED] chosen but not yet graded (quiz mode);
 * - [CORRECT]/[INCORRECT] post-grading feedback (green/red are reserved for exactly this per the SSOT).
 */
enum class OptionState { DEFAULT, SELECTED, CORRECT, INCORRECT }

/** A tappable answer option row. Stateless — the caller supplies the [state]. */
@Composable
fun AnswerOption(
    text: String,
    state: OptionState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = optionColors(state)
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = colors.container,
        contentColor = colors.content,
        border = colors.border,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = PmpSpacing.touchTargetMin),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
        )
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
            OptionColors(PmpCorrectContainer, PmpCorrect, BorderStroke(1.dp, PmpCorrect))
        OptionState.INCORRECT ->
            OptionColors(PmpIncorrectContainer, PmpIncorrect, BorderStroke(1.dp, PmpIncorrect))
    }
}
