package com.fax.passyourpmpexam.feature.free

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.fax.passyourpmpexam.core.designsystem.component.QuestionCard
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing
import com.fax.passyourpmpexam.core.domain.model.Domain
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeScreen(
    modifier: Modifier = Modifier,
    viewModel: FreeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // Pinned bar; the large title below it scrolls away (One UI style, matching Home/Settings).
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        FreeContent(
            state = state,
            onIntent = viewModel::onIntent,
            contentPadding = innerPadding,
        )
    }
}

@Composable
private fun FreeContent(
    state: FreeUiState,
    onIntent: (FreeIntent) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PmpSpacing.safeMargin)
            .padding(bottom = PmpSpacing.basePadding),
        verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
    ) {
        Text("Free Practice", style = MaterialTheme.typography.displayLarge)
        DomainFilterRow(
            available = state.availableDomains,
            selected = state.selectedDomains,
            onToggle = { onIntent(FreeIntent.ToggleDomain(it)) },
        )

        when {
            state.error != null -> ErrorState(
                message = state.error,
                actionLabel = "Try again",
                onAction = { onIntent(FreeIntent.Retry) },
            )
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
                if (state.answered) {
                    AnswerResultSheet(
                        isCorrect = state.isCorrect,
                        explanation = question.explanation,
                        onContinue = { onIntent(FreeIntent.Next) },
                    )
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
