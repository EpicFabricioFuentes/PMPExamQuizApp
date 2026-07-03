package com.fax.passyourpmpexam.feature.free

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.fax.passyourpmpexam.core.designsystem.component.AnswerFeedbackHaptics
import com.fax.passyourpmpexam.core.designsystem.component.AnswerOptionUi
import com.fax.passyourpmpexam.core.designsystem.component.EmptyState
import com.fax.passyourpmpexam.core.designsystem.component.ExplanationReveal
import com.fax.passyourpmpexam.core.designsystem.component.LoadingState
import com.fax.passyourpmpexam.core.designsystem.component.OptionState
import com.fax.passyourpmpexam.core.designsystem.component.PmpTopBar
import com.fax.passyourpmpexam.core.designsystem.component.PrimaryButton
import com.fax.passyourpmpexam.core.designsystem.component.QuestionCard
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing
import com.fax.passyourpmpexam.core.domain.model.Domain
import org.koin.androidx.compose.koinViewModel

@Composable
fun FreeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FreeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    Column(modifier = modifier.fillMaxSize()) {
        PmpTopBar(title = "Free Practice", onBack = onBack)
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            FreeContent(state = state, onIntent = viewModel::onIntent)
        }
    }
}

@Composable
private fun FreeContent(
    state: FreeUiState,
    onIntent: (FreeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PmpSpacing.safeMargin),
        verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
    ) {
        DomainFilterRow(
            available = state.availableDomains,
            selected = state.selectedDomains,
            onToggle = { onIntent(FreeIntent.ToggleDomain(it)) },
        )

        when {
            state.loading -> LoadingState(label = "Loading questions…")
            state.question == null -> EmptyState(
                title = "No matches",
                message = "No questions match the selected domains. Try clearing a filter.",
            )
            else -> {
                val question = state.question
                val options = question.options.mapIndexed { index, text ->
                    AnswerOptionUi(
                        text = text,
                        state = optionStateFor(
                            index = index,
                            answered = state.answered,
                            correctIndex = question.correctIndex,
                            selectedIndex = state.selectedIndex,
                        ),
                    )
                }
                QuestionCard(
                    questionText = question.text,
                    options = options,
                    onOptionSelected = { onIntent(FreeIntent.SelectOption(it)) },
                    optionsEnabled = !state.answered,
                )
                ExplanationReveal(visible = state.answered) {
                    Column(verticalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit)) {
                        Text(
                            text = if (state.isCorrect == true) "Correct!" else "Not quite — here's why.",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(text = question.explanation, style = MaterialTheme.typography.bodyMedium)
                        PrimaryButton(text = "Next question", onClick = { onIntent(FreeIntent.Next) })
                    }
                }
            }
        }

        // Buzz once when an answer is graded; silent while unanswered or between questions.
        AnswerFeedbackHaptics(isCorrect = state.isCorrect)
    }
}

@Composable
private fun DomainFilterRow(
    available: List<Domain>,
    selected: Set<Domain>,
    onToggle: (Domain) -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit),
    ) {
        available.forEach { domain ->
            FilterChip(
                selected = domain in selected,
                onClick = { onToggle(domain) },
                label = { Text(domain.displayName) },
            )
        }
    }
}

private fun optionStateFor(
    index: Int,
    answered: Boolean,
    correctIndex: Int,
    selectedIndex: Int?,
): OptionState = when {
    !answered -> if (index == selectedIndex) OptionState.SELECTED else OptionState.DEFAULT
    index == correctIndex -> OptionState.CORRECT
    index == selectedIndex -> OptionState.INCORRECT
    else -> OptionState.DEFAULT
}
