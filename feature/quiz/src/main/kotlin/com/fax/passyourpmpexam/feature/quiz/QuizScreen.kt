package com.fax.passyourpmpexam.feature.quiz

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.fax.passyourpmpexam.core.designsystem.component.AnswerOptionUi
import com.fax.passyourpmpexam.core.designsystem.component.EmptyState
import com.fax.passyourpmpexam.core.designsystem.component.LoadingState
import com.fax.passyourpmpexam.core.designsystem.component.OptionState
import com.fax.passyourpmpexam.core.designsystem.component.PmpTopBar
import com.fax.passyourpmpexam.core.designsystem.component.PrimaryButton
import com.fax.passyourpmpexam.core.designsystem.component.QuestionCard
import com.fax.passyourpmpexam.core.designsystem.theme.PmpCorrect
import com.fax.passyourpmpexam.core.designsystem.theme.PmpIncorrect
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing
import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.QuizType
import com.fax.passyourpmpexam.core.domain.model.ScoreResult
import com.fax.passyourpmpexam.core.domain.model.SessionQuestion
import org.koin.androidx.compose.koinViewModel

private val QUIZ_TYPES = listOf(
    QuizType.SHORT_10 to "10 Questions",
    QuizType.SHORT_25 to "25 Questions",
    QuizType.SHORT_50 to "50 Questions",
    QuizType.MOCK_180 to "Full Mock (180)",
)

@Composable
fun QuizScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuizViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val onIntent = viewModel::onIntent

    // Pause/resume the timer with the screen lifecycle so it accrues only while foregrounded.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.onForeground()
                Lifecycle.Event.ON_STOP -> viewModel.onBackground()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // While answering, back returns to the quiz setup screen; otherwise it leaves the Quiz section.
    val handleBack: () -> Unit = {
        if (state is QuizUiState.InProgress) onIntent(QuizIntent.ExitToSetup) else onBack()
    }
    Column(modifier = modifier.fillMaxSize()) {
        PmpTopBar(title = "Quiz", onBack = handleBack)
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val s = state) {
                QuizUiState.Loading -> LoadingState(label = "Loading quiz…")
                QuizUiState.Empty -> EmptyState(
                    title = "No questions yet",
                    message = "Your question bank is still being set up. Check back in a moment.",
                )
                is QuizUiState.ResumePrompt -> ResumePromptContent(s, onIntent)
                is QuizUiState.Setup -> SetupContent(s, onIntent)
                is QuizUiState.InProgress -> InProgressContent(s, onIntent)
                is QuizUiState.Results -> ResultsContent(s, onIntent)
            }
        }
    }
}

@Composable
private fun ResumePromptContent(
    state: QuizUiState.ResumePrompt,
    onIntent: (QuizIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PmpSpacing.basePadding),
        verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
    ) {
        Text("Resume your quiz?", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "You have an unfinished quiz on question " +
                "${state.session.currentIndex + 1} of ${state.session.questions.size}.",
            style = MaterialTheme.typography.bodyMedium,
        )
        PrimaryButton(text = "Resume", onClick = { onIntent(QuizIntent.ResumeSaved) })
        OutlinedButton(
            onClick = { onIntent(QuizIntent.DiscardSaved) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Discard and start over") }
    }
}

@Composable
private fun SetupContent(
    state: QuizUiState.Setup,
    onIntent: (QuizIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PmpSpacing.basePadding),
        verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
    ) {
        Text("Start a quiz", style = MaterialTheme.typography.headlineMedium)
        QUIZ_TYPES.forEach { (type, label) ->
            val selected = type == state.selectedType
            if (selected) {
                Button(onClick = { onIntent(QuizIntent.SelectType(type)) }, modifier = Modifier.fillMaxWidth()) {
                    Text(label)
                }
            } else {
                OutlinedButton(onClick = { onIntent(QuizIntent.SelectType(type)) }, modifier = Modifier.fillMaxWidth()) {
                    Text(label)
                }
            }
        }
        PrimaryButton(text = "Start", onClick = { onIntent(QuizIntent.Start) })
    }
}

@Composable
private fun InProgressContent(
    state: QuizUiState.InProgress,
    onIntent: (QuizIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val slot = state.session.questions[state.currentIndex]
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PmpSpacing.safeMargin),
        verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Question ${state.currentIndex + 1} of ${state.total}",
                style = MaterialTheme.typography.titleSmall,
            )
            state.remainingMillis?.let {
                Text(text = formatTime(it), style = MaterialTheme.typography.titleSmall)
            }
        }
        val progress by animateFloatAsState(
            targetValue = (state.currentIndex + 1).toFloat() / state.total,
            label = "quizProgress",
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )

        val options = slot.question.options.mapIndexed { index, text ->
            AnswerOptionUi(
                text = text,
                state = if (index == slot.selectedIndex) OptionState.SELECTED else OptionState.DEFAULT,
            )
        }
        QuestionCard(
            questionText = slot.question.text,
            options = options,
            onOptionSelected = { onIntent(QuizIntent.SelectOption(it)) },
            optionsEnabled = true,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
        ) {
            OutlinedButton(
                onClick = { onIntent(QuizIntent.Previous) },
                enabled = state.currentIndex > 0,
                modifier = Modifier.weight(1f),
            ) { Text("Previous") }

            if (state.currentIndex < state.total - 1) {
                Button(onClick = { onIntent(QuizIntent.Next) }, modifier = Modifier.weight(1f)) {
                    Text("Next")
                }
            } else {
                Button(onClick = { onIntent(QuizIntent.Submit) }, modifier = Modifier.weight(1f)) {
                    Text("Submit")
                }
            }
        }
    }
}

@Composable
private fun ResultsContent(
    state: QuizUiState.Results,
    onIntent: (QuizIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val result = state.result
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PmpSpacing.safeMargin),
        verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
    ) {
        Text(
            text = "${result.percent}%",
            style = MaterialTheme.typography.displayLarge,
        )
        Text(
            text = if (result.passed) "Passed" else "Not passed",
            style = MaterialTheme.typography.headlineMedium,
            color = if (result.passed) PmpCorrect else PmpIncorrect,
        )
        Text(
            text = "${result.correct} / ${result.total} correct" +
                (result.timeLimitMillis?.let { "  ·  ${formatTime(result.elapsedMillis)} of ${formatTime(it)}" } ?: ""),
            style = MaterialTheme.typography.bodyMedium,
        )

        Text("By domain", style = MaterialTheme.typography.titleSmall)
        result.perDomain.forEach { (domain, score) ->
            DomainScoreBar(domain, score.correct, score.total, score.percent)
        }

        PrimaryButton(text = "New quiz", onClick = { onIntent(QuizIntent.Restart) })

        Text("Review", style = MaterialTheme.typography.titleSmall)
        state.session.questions.forEach { slot ->
            ReviewItem(slot)
        }
    }
}

@Composable
private fun DomainScoreBar(domain: Domain, correct: Int, total: Int, percent: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit)) {
        Text(
            text = "${domain.displayName}: $correct / $total",
            style = MaterialTheme.typography.bodyMedium,
        )
        val fraction by animateFloatAsState(
            targetValue = percent / 100f,
            label = "domainScore",
        )
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ReviewItem(slot: SessionQuestion) {
    val correctIndex = slot.question.correctIndex
    val options = slot.question.options.mapIndexed { index, text ->
        val optionState = when {
            index == correctIndex -> OptionState.CORRECT
            index == slot.selectedIndex -> OptionState.INCORRECT
            else -> OptionState.DEFAULT
        }
        AnswerOptionUi(text = text, state = optionState)
    }
    Column(verticalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit)) {
        QuestionCard(
            questionText = slot.question.text,
            options = options,
            onOptionSelected = {},
            optionsEnabled = false,
        )
        Text(
            text = slot.question.explanation,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal,
        )
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
