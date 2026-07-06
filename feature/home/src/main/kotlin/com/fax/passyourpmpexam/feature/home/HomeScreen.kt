package com.fax.passyourpmpexam.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.fax.passyourpmpexam.core.ads.HomeBannerAd
import com.fax.passyourpmpexam.core.designsystem.component.PrimaryButton
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
    modifier: Modifier = Modifier,
) {
    // Collapsing "Good Morning" header: the greeting shrinks into the top bar as content scrolls up.
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(greeting()) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PmpSpacing.basePadding, vertical = PmpSpacing.itemGap),
            verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
        ) {
            StreakCard(streakCount = state.streakCount, dailyCompletedToday = state.dailyCompletedToday)

            PrimaryButton(
                text = if (state.dailyCompletedToday) "Review today's question" else "Daily Question",
                onClick = onStartDaily,
            )
            OutlinedButton(onClick = onStartQuiz, modifier = Modifier.fillMaxWidth()) {
                Text("Quiz")
            }
            OutlinedButton(onClick = onStartFree, modifier = Modifier.fillMaxWidth()) {
                Text("Free Practice")
            }

            HomeBannerAd(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun StreakCard(streakCount: Int, dailyCompletedToday: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(PmpSpacing.basePadding),
            verticalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit),
        ) {
            Text(
                text = if (streakCount > 0) "🔥 $streakCount-day streak" else "Start your streak today",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = if (dailyCompletedToday) {
                    "Today's question: done ✓"
                } else {
                    "Today's question is waiting"
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun greeting(): String = when (LocalTime.now().hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    in 17..21 -> "Good evening"
    else -> "Good night"
}
