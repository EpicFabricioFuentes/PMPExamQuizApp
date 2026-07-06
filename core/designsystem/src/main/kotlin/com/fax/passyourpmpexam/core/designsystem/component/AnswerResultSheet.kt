package com.fax.passyourpmpexam.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing
import kotlinx.coroutines.launch

/**
 * Slide-up result panel shown after an answer is graded (Free / Daily). Presents a status badge +
 * headline, the explanation, and a full-width continue action. [isCorrect] drives the tone:
 * true = correct (green), false = incorrect (red), null = neutral review (e.g. an already-completed
 * daily question). Both the button and swipe-to-dismiss invoke [onContinue].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswerResultSheet(
    isCorrect: Boolean?,
    explanation: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    continueLabel: String = "Continue",
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onContinue() }
    }
    ModalBottomSheet(
        onDismissRequest = onContinue,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = PmpSpacing.basePadding,
                    end = PmpSpacing.basePadding,
                    bottom = PmpSpacing.basePadding,
                ),
            verticalArrangement = Arrangement.spacedBy(PmpSpacing.basePadding),
        ) {
            ResultHeader(isCorrect)
            ExplanationCard(explanation)
            PrimaryButton(
                text = continueLabel,
                onClick = dismiss,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
            )
        }
    }
}

@Composable
private fun ResultHeader(isCorrect: Boolean?) {
    val scheme = MaterialTheme.colorScheme
    val badgeColor: Color
    val badgeContentColor: Color
    val icon: ImageVector
    val headline: String
    val headlineColor: Color
    when (isCorrect) {
        true -> {
            badgeColor = scheme.secondaryContainer
            badgeContentColor = scheme.onSecondaryContainer
            icon = Icons.Filled.Verified
            headline = "Excellent!"
            headlineColor = scheme.secondary
        }
        false -> {
            badgeColor = scheme.errorContainer
            badgeContentColor = scheme.onErrorContainer
            icon = Icons.Filled.Close
            headline = "Not quite"
            headlineColor = scheme.error
        }
        null -> {
            badgeColor = scheme.surfaceContainerHigh
            badgeContentColor = scheme.onSurfaceVariant
            icon = Icons.Filled.Verified
            headline = "Review"
            headlineColor = scheme.onSurface
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
    ) {
        Surface(shape = CircleShape, color = badgeColor, modifier = Modifier.size(48.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = badgeContentColor, modifier = Modifier.size(24.dp))
            }
        }
        Text(text = headline, style = MaterialTheme.typography.headlineMedium, color = headlineColor)
    }
}

@Composable
private fun ExplanationCard(explanation: String) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PmpSpacing.basePadding),
            verticalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit),
        ) {
            Text(text = "Explanation", style = MaterialTheme.typography.titleSmall)
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
