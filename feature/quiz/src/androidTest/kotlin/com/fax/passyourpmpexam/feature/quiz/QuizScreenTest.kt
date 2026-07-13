package com.fax.passyourpmpexam.feature.quiz

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fax.passyourpmpexam.core.common.IdGenerator
import com.fax.passyourpmpexam.core.common.TimeProvider
import com.fax.passyourpmpexam.core.designsystem.theme.PmpTheme
import com.fax.passyourpmpexam.core.domain.model.Attempt
import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.Question
import com.fax.passyourpmpexam.core.domain.model.QuizSession
import com.fax.passyourpmpexam.core.domain.model.QuizStatus
import com.fax.passyourpmpexam.core.domain.model.StreakState
import com.fax.passyourpmpexam.core.domain.repository.AttemptRepository
import com.fax.passyourpmpexam.core.domain.repository.QuestionRepository
import com.fax.passyourpmpexam.core.domain.repository.QuizSessionRepository
import com.fax.passyourpmpexam.core.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

/** Smoke test: the take-a-quiz flow reaches setup and starts an in-progress session. */
@RunWith(AndroidJUnit4::class)
class QuizScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun startingAQuizEntersTheInProgressPhase() {
        val pool = (0 until 12).map { index ->
            Question(
                id = "q$index",
                certificationId = "pmp",
                domain = Domain.PROCESS,
                text = "Question body $index",
                options = listOf("a", "b", "c", "d"),
                correctIndex = 0,
                explanation = "explanation",
                bankVersion = 1,
            )
        }
        val viewModel = QuizViewModel(
            questionRepository = FakeQuestionRepository(pool),
            attemptRepository = FakeAttemptRepository(),
            quizSessionRepository = FakeQuizSessionRepository(),
            streakRepository = FakeStreakRepository(),
            timeProvider = FakeTimeProvider,
            idGenerator = IdGenerator { "session-id" },
            savedStateHandle = SavedStateHandle(),
            random = Random(0),
        )

        composeTestRule.setContent {
            PmpTheme {
                QuizScreen(onBack = {}, viewModel = viewModel)
            }
        }

        // No persisted session -> the setup screen appears.
        composeTestRule.waitUntilTextAppears("Quiz Mode")
        composeTestRule.onNodeWithText("Start Quiz Session").assertIsDisplayed()

        composeTestRule.onNodeWithText("Start Quiz Session").performClick()

        // SHORT_10 is the default, so 10 questions are drawn from the 12-question pool.
        composeTestRule.waitUntilTextAppears("Question 1 of 10")
        composeTestRule.onNodeWithText("Question 1 of 10").assertIsDisplayed()
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

private class FakeQuestionRepository(private val pool: List<Question>) : QuestionRepository {
    override suspend fun getAll(certificationId: String): List<Question> = pool
    override suspend fun getById(id: String): Question? = pool.firstOrNull { it.id == id }
    override suspend fun count(): Int = pool.size
    override suspend fun upsertAll(questions: List<Question>) = Unit
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

private class FakeQuizSessionRepository : QuizSessionRepository {
    private val saved = LinkedHashMap<String, QuizSession>()
    override suspend fun save(session: QuizSession) {
        saved[session.id] = session
    }
    override suspend fun getById(id: String): QuizSession? = saved[id]
    override suspend fun getLatestByStatus(status: QuizStatus): QuizSession? =
        saved.values.lastOrNull { it.status == status }
}

private class FakeStreakRepository : StreakRepository {
    private var state = StreakState()
    override fun observe(): Flow<StreakState> = flowOf(state)
    override suspend fun get(): StreakState = state
    override suspend fun set(state: StreakState) {
        this.state = state
    }
}
