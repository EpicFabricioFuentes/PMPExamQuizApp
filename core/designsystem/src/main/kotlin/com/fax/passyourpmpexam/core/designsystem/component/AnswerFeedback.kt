package com.fax.passyourpmpexam.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Fires a one-shot haptic when an answer is graded: a confirm tick for [isCorrect] == true and a
 * reject buzz for false. No-op while [isCorrect] is null (unanswered), so re-entering an
 * already-answered screen or advancing to a fresh question does not buzz.
 */
@Composable
fun AnswerFeedbackHaptics(isCorrect: Boolean?) {
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(isCorrect) {
        when (isCorrect) {
            true -> haptics.performHapticFeedback(HapticFeedbackType.Confirm)
            false -> haptics.performHapticFeedback(HapticFeedbackType.Reject)
            null -> Unit
        }
    }
}

/**
 * Reveals post-answer explanation content with a fade + vertical expand, matching the SSOT's
 * sliding-explanation motion. Exit is instantaneous so advancing to the next question (which swaps
 * the underlying content) never animates stale text out. Must be used within a [ColumnScope].
 */
@Composable
fun ColumnScope.ExplanationReveal(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn() + expandVertically(),
        exit = ExitTransition.None,
    ) {
        content()
    }
}
