package com.fax.passyourpmpexam.feature.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.fax.passyourpmpexam.core.designsystem.component.AnswerFeedbackHaptics
import com.fax.passyourpmpexam.core.designsystem.component.AnswerOptionUi
import com.fax.passyourpmpexam.core.designsystem.component.AnswerResultSheet
import com.fax.passyourpmpexam.core.designsystem.component.EmptyState
import com.fax.passyourpmpexam.core.designsystem.component.ErrorState
import com.fax.passyourpmpexam.core.designsystem.component.LoadingState
import com.fax.passyourpmpexam.core.designsystem.component.OptionState
import com.fax.passyourpmpexam.core.designsystem.component.PmpTopBar
import com.fax.passyourpmpexam.core.designsystem.component.QuestionCard
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun DailyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DailyViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    Column(modifier = modifier.fillMaxSize()) {
        PmpTopBar(title = "Daily Question", onBack = onBack)
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            DailyContent(
                state = state,
                onOptionSelected = { viewModel.onIntent(DailyIntent.SelectOption(it)) },
                onDone = onBack,
                onRetry = { viewModel.onIntent(DailyIntent.Retry) },
            )
        }
    }
}

@Composable
private fun DailyContent(
    state: DailyUiState,
    onOptionSelected: (Int) -> Unit,
    onDone: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        DailyUiState.Loading -> LoadingState(label = "Loading today's question…")
        DailyUiState.Empty -> EmptyState(
            title = "No question yet",
            message = "Your question bank is still being set up. Check back in a moment.",
        )
        is DailyUiState.Error -> ErrorState(
            message = state.message,
            actionLabel = "Try again",
            onAction = onRetry,
        )
        is DailyUiState.Ready -> ReadyContent(state, onOptionSelected, onDone, modifier)
    }
}

@Composable
private fun ReadyContent(
    state: DailyUiState.Ready,
    onOptionSelected: (Int) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val question = state.question
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PmpSpacing.safeMargin),
        verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
    ) {
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
            onOptionSelected = onOptionSelected,
            optionsEnabled = !state.answered,
        )

        if (state.answered) {
            // A previously-completed question that the user is only reviewing has no fresh grade,
            // so present it neutrally rather than as a correct/incorrect result.
            val reviewing = state.alreadyCompletedToday && state.selectedIndex == null
            AnswerResultSheet(
                isCorrect = if (reviewing) null else state.isCorrect,
                explanation = question.explanation,
                onContinue = onDone,
                continueLabel = "Done",
            )
        }
    }

    // Buzz once when a fresh answer is graded (null on load / already-completed → silent).
    AnswerFeedbackHaptics(isCorrect = state.isCorrect)
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
