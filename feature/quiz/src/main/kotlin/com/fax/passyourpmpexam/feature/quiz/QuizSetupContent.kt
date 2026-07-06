package com.fax.passyourpmpexam.feature.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
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
import com.fax.passyourpmpexam.core.designsystem.component.PrimaryButton
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing
import com.fax.passyourpmpexam.core.designsystem.theme.PmpTheme
import com.fax.passyourpmpexam.core.domain.model.QuizType

/** Question counts offered by the Short Quiz card, in display order. */
private val SHORT_TYPES = listOf(QuizType.SHORT_10, QuizType.SHORT_25, QuizType.SHORT_50)

private val QuizType.shortChipLabel: String
    get() = "${questionCount} Q"

/**
 * The quiz-mode selection screen. A large "Quiz Mode" header over two selectable cards — a Short
 * Quiz card with question-count chips and an estimated time, and a Full Mock Exam card with a stats
 * grid — plus a pinned "Start Quiz Session" CTA. Selection maps directly onto [QuizType] via the
 * existing [QuizIntent.SelectType]; the CTA fires [QuizIntent.Start].
 */
@Composable
internal fun SetupContent(
    state: QuizUiState.Setup,
    onIntent: (QuizIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedType = state.selectedType
    val mockSelected = selectedType == QuizType.MOCK_180
    // The chip/estimate reflect the active short type; default to 10 Q when the mock is selected.
    val activeShortType = if (mockSelected) QuizType.SHORT_10 else selectedType

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PmpSpacing.safeMargin),
            verticalArrangement = Arrangement.spacedBy(PmpSpacing.basePadding),
        ) {
            SetupHeader()
            ShortQuizCard(
                selected = !mockSelected,
                activeShortType = activeShortType,
                onSelectType = { onIntent(QuizIntent.SelectType(it)) },
                onSelectCard = { onIntent(QuizIntent.SelectType(activeShortType)) },
            )
            FullMockCard(
                selected = mockSelected,
                onSelect = { onIntent(QuizIntent.SelectType(QuizType.MOCK_180)) },
            )
        }
        PrimaryButton(
            text = "Start Quiz Session",
            onClick = { onIntent(QuizIntent.Start) },
            modifier = Modifier.padding(PmpSpacing.safeMargin),
        )
    }
}

@Composable
private fun SetupHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = PmpSpacing.basePadding),
        verticalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit * 2),
    ) {
        Text("Quiz Mode", style = MaterialTheme.typography.displayLarge)
        Text(
            text = "Select your session intensity to begin your PMP certification journey.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ShortQuizCard(
    selected: Boolean,
    activeShortType: QuizType,
    onSelectType: (QuizType) -> Unit,
    onSelectCard: () -> Unit,
) {
    SetupCard(selected = selected, onClick = onSelectCard) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SetupIconTile(
                icon = Icons.Filled.Timer,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                background = MaterialTheme.colorScheme.secondaryContainer,
            )
            Spacer(Modifier.weight(1f))
            SetupPill(
                text = "Recommended",
                container = MaterialTheme.colorScheme.surfaceContainerHigh,
                content = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text("Short Quiz", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Perfect for quick review sessions and targeted domain practice.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap)) {
            SHORT_TYPES.forEach { type ->
                CountChip(
                    label = type.shortChipLabel,
                    selected = selected && type == activeShortType,
                    onClick = { onSelectType(type) },
                )
            }
        }
        InfoRow(
            icon = Icons.Filled.Schedule,
            text = "Est: ${activeShortType.timeLimitMinutes} mins",
        )
    }
}

@Composable
private fun FullMockCard(
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val mock = QuizType.MOCK_180
    SetupCard(selected = selected, onClick = onSelect) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SetupIconTile(
                icon = Icons.Filled.Assignment,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                background = MaterialTheme.colorScheme.tertiaryContainer,
            )
            Spacer(Modifier.weight(1f))
            SetupPill(
                text = "Intensive",
                container = MaterialTheme.colorScheme.errorContainer,
                content = MaterialTheme.colorScheme.onErrorContainer,
                bold = true,
            )
        }
        Text("Full Mock Exam", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Simulate the real PMP experience with a timed, high-stakes assessment.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap)) {
            MockStatTile(label = "Questions", value = "${mock.questionCount}", modifier = Modifier.weight(1f))
            MockStatTile(label = "Time Limit", value = "${mock.timeLimitMinutes} m", modifier = Modifier.weight(1f))
        }
        InfoRow(icon = Icons.Filled.History, text = "Requires ~4 hours of focus")
    }
}

/**
 * Shared card container for a quiz mode: a rounded, tonal surface with a 2dp border that turns
 * primary when the mode is selected. Its [content] is stacked in a spaced column.
 */
@Composable
private fun SetupCard(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(
                width = 2.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                },
                shape = MaterialTheme.shapes.extraLarge,
            )
            .clickable(onClick = onClick)
            .padding(PmpSpacing.basePadding),
        verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
        content = content,
    )
}

@Composable
private fun SetupIconTile(icon: ImageVector, tint: Color, background: Color) {
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

@Composable
private fun SetupPill(text: String, container: Color, content: Color, bold: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = content,
        fontWeight = if (bold) FontWeight.SemiBold else null,
        modifier = Modifier
            .clip(CircleShape)
            .background(container)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

@Composable
private fun CountChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val container = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = contentColor,
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .background(container)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
    )
}

@Composable
private fun MockStatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(PmpSpacing.safeMargin),
        verticalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = MaterialTheme.typography.headlineMedium, maxLines = 1)
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "Setup — short selected")
@Composable
private fun SetupContentShortPreview() {
    PmpTheme {
        SetupContent(state = QuizUiState.Setup(QuizType.SHORT_25), onIntent = {})
    }
}

@Preview(name = "Setup — mock selected")
@Composable
private fun SetupContentMockPreview() {
    PmpTheme {
        SetupContent(state = QuizUiState.Setup(QuizType.MOCK_180), onIntent = {})
    }
}
