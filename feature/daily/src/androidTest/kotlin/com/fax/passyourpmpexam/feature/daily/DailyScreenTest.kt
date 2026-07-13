package com.fax.passyourpmpexam.feature.daily

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fax.passyourpmpexam.core.common.IdGenerator
import com.fax.passyourpmpexam.core.common.TimeProvider
import com.fax.passyourpmpexam.core.designsystem.theme.PmpTheme
import com.fax.passyourpmpexam.core.domain.model.Attempt
import com.fax.passyourpmpexam.core.domain.model.DailyGoal
import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.Question
import com.fax.passyourpmpexam.core.domain.model.StreakState
import com.fax.passyourpmpexam.core.domain.model.ThemeMode
import com.fax.passyourpmpexam.core.domain.repository.AttemptRepository
import com.fax.passyourpmpexam.core.domain.repository.QuestionRepository
import com.fax.passyourpmpexam.core.domain.repository.SettingsRepository
import com.fax.passyourpmpexam.core.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Smoke test: the daily-question flow renders the question and reveals feedback after answering. */
@RunWith(AndroidJUnit4::class)
class DailyScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun answeringTheDailyQuestionRevealsFeedback() {
        val question = Question(
            id = "q1",
            certificationId = "pmp",
            domain = Domain.PROCESS,
            text = "What is the critical path?",
            options = listOf("Longest path", "Shortest path", "Cheapest path", "Random path"),
            correctIndex = 0,
            explanation = "The critical path is the longest sequence of dependent activities.",
            bankVersion = 1,
        )
        val viewModel = DailyViewModel(
            questionRepository = FakeQuestionRepository(listOf(question)),
            attemptRepository = FakeAttemptRepository(),
            streakRepository = FakeStreakRepository(),
            settingsRepository = FakeSettingsRepository(),
            timeProvider = FakeTimeProvider,
            idGenerator = IdGenerator { "attempt-id" },
        )

        composeTestRule.setContent {
            PmpTheme {
                DailyScreen(onBack = {}, viewModel = viewModel)
            }
        }

        composeTestRule.waitUntilTextAppears("What is the critical path?")
        composeTestRule.onNodeWithText("Longest path").assertIsDisplayed()

        composeTestRule.onNodeWithText("Longest path").performClick()

        // After answering, the daily result sheet exposes a "Done" action.
        composeTestRule.waitUntilTextAppears("Done")
        composeTestRule.onNodeWithText("Done").assertIsDisplayed()
    }
}

private fun ComposeContentTestRule.waitUntilTextAppears(text: String, timeoutMillis: Long = 5_000L) {
    waitUntil(timeoutMillis) {
        onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }
}

private object FakeTimeProvider : TimeProvider {
    override fun todayEpochDay(): Long = 100L
    override fun nowMillis(): Long = 5_000L
}

private class FakeQuestionRepository(private var questions: List<Question>) : QuestionRepository {
    override suspend fun getAll(certificationId: String): List<Question> = questions
    override suspend fun getById(id: String): Question? = questions.firstOrNull { it.id == id }
    override suspend fun count(): Int = questions.size
    override suspend fun upsertAll(questions: List<Question>) {
        this.questions = questions
    }
}

private class FakeAttemptRepository : AttemptRepository {
    private val recorded = mutableListOf<Attempt>()
    override suspend fun record(attempt: Attempt) {
        recorded += attempt
    }
    override suspend fun recordAll(attempts: List<Attempt>) {
        recorded += attempts
    }
    override fun observeAll(): Flow<List<Attempt>> = flowOf(recorded.toList())
    override suspend fun totalCount(): Int = recorded.size
    override suspend fun correctCount(): Int = recorded.count { it.isCorrect }
    override fun observeAnsweredCountBetween(startMillis: Long, endMillis: Long): Flow<Int> =
        flowOf(recorded.count { it.answeredAt in startMillis until endMillis })
}

private class FakeStreakRepository : StreakRepository {
    private var state = StreakState()
    override fun observe(): Flow<StreakState> = flowOf(state)
    override suspend fun get(): StreakState = state
    override suspend fun set(state: StreakState) {
        this.state = state
    }
}

private class FakeSettingsRepository : SettingsRepository {
    private var dailyLastAnswered = -1L
    private var installedBankVersion = 0
    override fun observeThemeMode(): Flow<ThemeMode> = flowOf(ThemeMode.SYSTEM)
    override suspend fun setThemeMode(mode: ThemeMode) = Unit
    override fun observeReminderEnabled(): Flow<Boolean> = flowOf(false)
    override suspend fun setReminderEnabled(enabled: Boolean) = Unit
    override fun observeReminderMinuteOfDay(): Flow<Int> = flowOf(0)
    override suspend fun setReminderMinuteOfDay(minuteOfDay: Int) = Unit
    override fun observeDailyGoal(): Flow<Int> = flowOf(DailyGoal.DEFAULT)
    override suspend fun setDailyGoal(goal: Int) = Unit
    override suspend fun getInstalledBankVersion(): Int = installedBankVersion
    override suspend fun setInstalledBankVersion(version: Int) {
        installedBankVersion = version
    }
    override fun observeHasCompletedFirstRun(): Flow<Boolean> = flowOf(true)
    override suspend fun setFirstRunCompleted() = Unit
    override suspend fun getDailyLastAnsweredEpochDay(): Long = dailyLastAnswered
    override suspend fun setDailyLastAnsweredEpochDay(epochDay: Long) {
        dailyLastAnswered = epochDay
    }
}
