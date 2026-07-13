package com.fax.passyourpmpexam.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fax.passyourpmpexam.core.ads.HomeBannerAd
import com.fax.passyourpmpexam.core.designsystem.component.ErrorState
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing
import org.koin.androidx.compose.koinViewModel
import java.time.LocalTime

@Composable
fun HomeScreen(
    onStartDaily: () -> Unit,
    onStartQuiz: () -> Unit,
    onStartFree: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    HomeContent(
        state = state,
        onStartDaily = onStartDaily,
        onStartQuiz = onStartQuiz,
        onStartFree = onStartFree,
        onRetry = viewModel::onRetry,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeUiState,
    onStartDaily: () -> Unit,
    onStartQuiz: () -> Unit,
    onStartFree: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // Pinned brand bar: logo top-left, wordmark on the right. The greeting
            // below it scrolls away (One UI style).
            TopAppBar(
                title = {},
                navigationIcon = {
                    Image(
                        // Decorative: the adjacent "PMP Prep" wordmark conveys the brand to TalkBack.
                        painter = painterResource(R.drawable.pmp_logo),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).padding(start = 16.dp),
                    )
                },
                actions = {
                    Wordmark()
                    Spacer(Modifier.width(PmpSpacing.safeMargin))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        if (state.error != null) {
            ErrorState(
                message = state.error,
                modifier = Modifier.padding(innerPadding),
                actionLabel = "Try again",
                onAction = onRetry,
            )
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PmpSpacing.safeMargin)
                .padding(bottom = PmpSpacing.basePadding),
            verticalArrangement = Arrangement.spacedBy(PmpSpacing.safeMargin),
        ) {
            GreetingBlock()
            StatusRow(state)
            Text(
                text = "LEARNING MODES",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = PmpSpacing.gridUnit),
            )
            DailyModeCard(onStartDaily)
            QuizModeCard(questionCount = state.questionCount, onClick = onStartQuiz)
            FreeModeCard(onStartFree)
            HomeBannerAd(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun GreetingBlock() {
    Column(verticalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit)) {
        Text(text = greeting(), style = MaterialTheme.typography.displayLarge)
        Text(
            text = "Ready to crush your PMP goal today?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatusRow(state: HomeUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap)) {
        StatusCard(
            icon = Icons.Filled.Whatshot,
            iconTint = MaterialTheme.colorScheme.tertiary,
            label = "Current",
            value = "${state.streakCount} day streak",
            progress = weeklyStreakProgress(state.streakCount),
            progressColor = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f),
        )
        StatusCard(
            icon = Icons.Filled.Quiz,
            iconTint = MaterialTheme.colorScheme.primary,
            label = "Daily Goal",
            value = "${state.answeredToday}/${state.dailyGoal} Done",
            progress = if (state.dailyGoal <= 0) 0f else state.answeredToday / state.dailyGoal.toFloat(),
            progressColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DailyModeCard(onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    ModeCard(
        chipText = "EASY WIN",
        title = "Daily Question",
        body = "One focused question to keep your momentum alive.",
        icon = Icons.Filled.CalendarToday,
        ctaText = "Start now",
        // Always-violet hero with white foreground, consistent across light/dark themes.
        style = ModeCardStyle(
            containerColor = cs.primaryContainer,
            contentColor = Color.White,
            bodyColor = Color.White.copy(alpha = 0.85f),
            chipContainer = Color.White.copy(alpha = 0.2f),
            chipContent = Color.White,
            iconTint = Color.White,
            iconTileColor = Color.White.copy(alpha = 0.15f),
            ctaColor = Color.White,
        ),
        onClick = onClick,
    )
}

@Composable
private fun QuizModeCard(questionCount: Int, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    ModeCard(
        chipText = "INTENSIVE",
        title = "Quiz Mode",
        body = "Simulate real exam conditions with timed sessions.",
        icon = Icons.Filled.Timer,
        ctaText = if (questionCount > 0) "$questionCount Questions available" else "Timed practice sessions",
        style = ModeCardStyle(
            containerColor = cs.surfaceContainerHigh,
            contentColor = cs.onSurface,
            bodyColor = cs.onSurfaceVariant,
            chipContainer = cs.secondaryContainer,
            chipContent = cs.onSecondaryContainer,
            iconTint = cs.primary,
            iconTileColor = cs.primary.copy(alpha = 0.08f),
            ctaColor = cs.primary,
        ),
        onClick = onClick,
    )
}

@Composable
private fun FreeModeCard(onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    ModeCard(
        chipText = "ADAPTIVE",
        title = "Free Mode",
        body = "Practice at your own pace without time pressure.",
        icon = Icons.Filled.Psychology,
        ctaText = "Topic based practice",
        style = ModeCardStyle(
            containerColor = cs.surfaceContainerHigh,
            contentColor = cs.onSurface,
            bodyColor = cs.onSurfaceVariant,
            chipContainer = cs.tertiaryContainer,
            chipContent = cs.onTertiaryContainer,
            iconTint = cs.tertiary,
            iconTileColor = cs.tertiary.copy(alpha = 0.08f),
            ctaColor = cs.tertiary,
        ),
        onClick = onClick,
    )
}

/** Streak fill toward a rolling 7-day milestone (a full bar every 7th day). */
private fun weeklyStreakProgress(streak: Int): Float =
    if (streak <= 0) 0f else ((streak - 1) % 7 + 1) / 7f

private fun greeting(): String = when (LocalTime.now().hour) {
    in 5..11 -> "Good Morning"
    in 12..16 -> "Good Afternoon"
    in 17..21 -> "Good Evening"
    else -> "Good Night"
}
