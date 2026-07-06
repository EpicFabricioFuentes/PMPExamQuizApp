package com.fax.passyourpmpexam.feature.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.fax.passyourpmpexam.core.designsystem.component.AnswerOptionUi
import com.fax.passyourpmpexam.core.designsystem.component.CircularScoreRing
import com.fax.passyourpmpexam.core.designsystem.component.EmptyState
import com.fax.passyourpmpexam.core.designsystem.component.LoadingState
import com.fax.passyourpmpexam.core.designsystem.component.OptionState
import com.fax.passyourpmpexam.core.designsystem.component.PmpTopBar
import com.fax.passyourpmpexam.core.designsystem.component.PrimaryButton
import com.fax.passyourpmpexam.core.designsystem.component.QuestionCard
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing
import com.fax.passyourpmpexam.core.designsystem.theme.PmpTheme
import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.DomainScore
import com.fax.passyourpmpexam.core.domain.model.Question
import com.fax.passyourpmpexam.core.domain.model.QuizConfig
import com.fax.passyourpmpexam.core.domain.model.QuizSession
import com.fax.passyourpmpexam.core.domain.model.QuizType
import com.fax.passyourpmpexam.core.domain.model.ScoreResult
import com.fax.passyourpmpexam.core.domain.model.SessionQuestion
import com.fax.passyourpmpexam.core.domain.scoring.Scorer
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

    // Leaving an in-progress quiz (top-bar arrow or system back / gesture) asks for confirmation;
    // from any other phase, back just leaves the Quiz section.
    var showLeaveDialog by remember { mutableStateOf(false) }
    val handleBack: () -> Unit = {
        if (state is QuizUiState.InProgress) showLeaveDialog = true else onBack()
    }
    BackHandler(enabled = state is QuizUiState.InProgress && !showLeaveDialog) {
        showLeaveDialog = true
    }
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave the quiz?") },
            text = { Text("You can resume it later from where you left off.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveDialog = false
                        onBack()
                    },
                ) { Text("Leave") }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) { Text("Stay") }
            },
        )
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
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(PmpSpacing.safeMargin),
            verticalArrangement = Arrangement.spacedBy(PmpSpacing.basePadding),
        ) {
            ResultHero(result)
            DomainSection(result)
            StatRow(result)
            ReviewSection(state.session.questions)
        }
        PrimaryButton(
            text = "Retake Similar Quiz",
            onClick = { onIntent(QuizIntent.Restart) },
            modifier = Modifier.padding(PmpSpacing.safeMargin),
        )
    }
}

@Composable
private fun ResultHero(result: ScoreResult) {
    val accent = if (result.passed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
    ) {
        CircularScoreRing(progress = result.percent / 100f, ringColor = accent) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${result.percent}%", style = MaterialTheme.typography.displayLarge)
                Text(
                    text = if (result.passed) "PASS" else "FAIL",
                    style = MaterialTheme.typography.titleSmall,
                    color = accent,
                )
            }
        }
        Text(
            text = if (result.passed) {
                "Great work — you cleared the ${Scorer.PASS_THRESHOLD_PERCENT}% pass mark for this practice quiz."
            } else {
                "You scored ${result.percent}%. Keep practicing — the pass mark is ${Scorer.PASS_THRESHOLD_PERCENT}%."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DomainSection(result: ScoreResult) {
    Column(verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap)) {
        SectionHeader("Domain Performance")
        result.perDomain.forEach { (domain, score) ->
            DomainCard(domain, score)
        }
    }
}

@Composable
private fun DomainCard(domain: Domain, score: DomainScore) {
    val accent = domainAccentColor(score.percent)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.large,
            )
            .padding(PmpSpacing.safeMargin),
        verticalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(domain.displayName, style = MaterialTheme.typography.headlineMedium)
            Text("${score.percent}%", style = MaterialTheme.typography.bodyLarge, color = accent)
        }
        val fraction by animateFloatAsState(targetValue = score.percent / 100f, label = "domainBar")
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = accent,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
    }
}

@Composable
private fun StatRow(result: ScoreResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
    ) {
        val pace = paceLabel(result.elapsedMillis, result.timeLimitMillis)
        StatTile(
            label = "Total Time",
            value = formatTime(result.elapsedMillis),
            sub = pace?.first,
            subColor = pace?.second ?: MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        StatTile(
            label = "Accuracy",
            value = "${result.correct}/${result.total}",
            sub = "Correct Answers",
            subColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    sub: String?,
    subColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(PmpSpacing.safeMargin),
        verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column {
            Text(value, style = MaterialTheme.typography.headlineMedium, maxLines = 1)
            if (sub != null) {
                Text(sub, style = MaterialTheme.typography.labelSmall, color = subColor)
            }
        }
    }
}

@Composable
private fun ReviewSection(questions: List<SessionQuestion>) {
    Column(verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap)) {
        SectionHeader("Review All Questions")
        questions.forEach { slot ->
            ReviewRow(slot)
        }
    }
}

@Composable
private fun ReviewRow(slot: SessionQuestion) {
    var expanded by remember { mutableStateOf(false) }
    val correct = slot.isCorrect == true
    val circleBg = if (correct) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
    val circleFg = if (correct) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.error
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.large,
            )
            .animateContentSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(PmpSpacing.itemGap),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(circleBg),
            ) {
                Icon(
                    imageVector = if (correct) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = null,
                    tint = circleFg,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = slot.question.text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = slot.question.domain.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.rotate(if (expanded) 180f else 0f),
            )
        }
        if (expanded) {
            val correctIndex = slot.question.correctIndex
            val options = slot.question.options.mapIndexed { index, text ->
                val optionState = when {
                    index == correctIndex -> OptionState.CORRECT
                    index == slot.selectedIndex -> OptionState.INCORRECT
                    else -> OptionState.DEFAULT
                }
                AnswerOptionUi(text = text, state = optionState)
            }
            Column(
                modifier = Modifier.padding(
                    start = PmpSpacing.itemGap,
                    end = PmpSpacing.itemGap,
                    bottom = PmpSpacing.itemGap,
                ),
                verticalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit),
            ) {
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
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun domainAccentColor(percent: Int): Color = when {
    percent >= 80 -> MaterialTheme.colorScheme.secondary
    percent >= Scorer.PASS_THRESHOLD_PERCENT -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.error
}

@Composable
private fun paceLabel(elapsedMillis: Long, timeLimitMillis: Long?): Pair<String, Color>? {
    if (timeLimitMillis == null || timeLimitMillis <= 0L) return null
    val ratio = elapsedMillis.toFloat() / timeLimitMillis
    return when {
        ratio <= 0.6f -> "Fast Pace" to MaterialTheme.colorScheme.secondary
        ratio <= 0.9f -> "Steady Pace" to MaterialTheme.colorScheme.onSurfaceVariant
        else -> "Close to Limit" to MaterialTheme.colorScheme.tertiary
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

private fun previewQuestion(domain: Domain, text: String) = Question(
    id = text,
    certificationId = "pmp",
    domain = domain,
    text = text,
    options = listOf("Initiating", "Planning", "Executing", "Closing"),
    correctIndex = 3,
    explanation = "The Closing process group formally completes or closes the project or phase.",
    bankVersion = 1,
)

@Preview(name = "Results — pass")
@Composable
private fun ResultsContentPreview() {
    val session = QuizSession(
        id = "preview",
        config = QuizConfig(QuizType.SHORT_50),
        questions = listOf(
            SessionQuestion(previewQuestion(Domain.PROCESS, "Project Charter approval flow and its key stakeholders"), selectedIndex = 1),
            SessionQuestion(previewQuestion(Domain.PEOPLE, "Conflict management strategies for a distributed team"), selectedIndex = 3),
            SessionQuestion(previewQuestion(Domain.BUSINESS_ENVIRONMENT, "Applying the stakeholder salience model"), selectedIndex = 3),
        ),
    )
    val result = ScoreResult(
        correct = 39,
        total = 50,
        percent = 78,
        passed = true,
        perDomain = mapOf(
            Domain.PEOPLE to DomainScore(17, 20),
            Domain.PROCESS to DomainScore(18, 25),
            Domain.BUSINESS_ENVIRONMENT to DomainScore(4, 5),
        ),
        elapsedMillis = 42L * 60_000 + 15_000,
        timeLimitMillis = 64L * 60_000,
    )
    PmpTheme {
        ResultsContent(state = QuizUiState.Results(result, session), onIntent = {})
    }
}
