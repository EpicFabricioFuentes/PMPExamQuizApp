package com.fax.passyourpmpexam.feature.quiz

import androidx.lifecycle.SavedStateHandle
import com.fax.passyourpmpexam.core.common.IdGenerator
import com.fax.passyourpmpexam.core.common.TimeProvider
import com.fax.passyourpmpexam.core.domain.model.Attempt
import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.Question
import com.fax.passyourpmpexam.core.domain.model.QuizConfig
import com.fax.passyourpmpexam.core.domain.model.QuizSession
import com.fax.passyourpmpexam.core.domain.model.QuizStatus
import com.fax.passyourpmpexam.core.domain.model.QuizType
import com.fax.passyourpmpexam.core.domain.model.SessionQuestion
import com.fax.passyourpmpexam.core.domain.model.StreakState
import com.fax.passyourpmpexam.core.domain.repository.AttemptRepository
import com.fax.passyourpmpexam.core.domain.repository.QuestionRepository
import com.fax.passyourpmpexam.core.domain.repository.QuizSessionRepository
import com.fax.passyourpmpexam.core.domain.repository.StreakRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var questions: FakeQuestionRepository
    private lateinit var attempts: FakeAttemptRepository
    private lateinit var sessions: FakeQuizSessionRepository
    private lateinit var streak: FakeStreakRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        questions = FakeQuestionRepository()
        attempts = FakeAttemptRepository()
        sessions = FakeQuizSessionRepository()
        streak = FakeStreakRepository()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(handle: SavedStateHandle = SavedStateHandle()) = QuizViewModel(
        questionRepository = questions,
        attemptRepository = attempts,
        quizSessionRepository = sessions,
        streakRepository = streak,
        timeProvider = FakeTimeProvider,
        idGenerator = IdGenerator { "id" },
        savedStateHandle = handle,
        random = Random(0),
    )

    @Test
    fun startBuildsSessionWithRequestedCount() = runTest(dispatcher) {
        questions.questions = (0 until 15).map { question("q$it") }

        val vm = viewModel()
        advanceUntilIdle() // load pool -> Setup

        vm.onIntent(QuizIntent.Start)

        val state = vm.state.value
        assertTrue(state is QuizUiState.InProgress)
        assertEquals(10, state.session.questions.size) // SHORT_10 default
    }

    @Test
    fun answeringAllCorrectlyProducesPassingResultAndRecordsAttempts() = runTest(dispatcher) {
        questions.questions = (0 until 4).map { question("q$it", correctIndex = 0) }

        val vm = viewModel()
        advanceUntilIdle()
        vm.onIntent(QuizIntent.Start)

        val total = (vm.state.value as QuizUiState.InProgress).total // 4 (pool smaller than 10)
        repeat(total) { index ->
            vm.onIntent(QuizIntent.SelectOption(0))
            if (index < total - 1) vm.onIntent(QuizIntent.Next)
        }
        vm.onIntent(QuizIntent.Submit)
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is QuizUiState.Results)
        assertEquals(100, state.result.percent)
        assertTrue(state.result.passed)
        assertEquals(total, attempts.recorded.size)
        assertEquals(1, streak.state.currentStreak)
    }

    @Test
    fun timerExpiryAutoSubmits() = runTest(dispatcher) {
        questions.questions = (0 until 10).map { question("q$it") }

        val vm = viewModel()
        advanceUntilIdle()
        vm.onIntent(QuizIntent.Start) // SHORT_10 -> 13 minute limit

        advanceTimeBy(13 * 60 * 1000L + 2_000L)
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is QuizUiState.Results)
        assertEquals(0, state.result.percent) // nothing answered
    }

    @Test
    fun offersResumePromptForPersistedInProgressSessionThenResumes() = runTest(dispatcher) {
        questions.questions = (0 until 5).map { question("q$it") }
        val existing = QuizSession(
            id = "s1",
            config = QuizConfig(QuizType.SHORT_10),
            questions = listOf(SessionQuestion(question("q0"))),
            status = QuizStatus.IN_PROGRESS,
            currentIndex = 0,
            elapsedMillis = 3_000L,
            createdAt = 1L,
        )
        sessions.save(existing)

        val vm = viewModel()
        advanceUntilIdle()
        assertTrue(vm.state.value is QuizUiState.ResumePrompt)

        vm.onIntent(QuizIntent.ResumeSaved)
        val resumed = vm.state.value
        assertTrue(resumed is QuizUiState.InProgress)
        assertEquals("s1", resumed.session.id)
    }

    private fun question(id: String, correctIndex: Int = 0) = Question(
        id = id,
        certificationId = "PMP",
        domain = Domain.PROCESS,
        text = "text",
        options = listOf("a", "b", "c", "d"),
        correctIndex = correctIndex,
        explanation = "explanation",
        bankVersion = 1,
    )
}

private object FakeTimeProvider : TimeProvider {
    override fun todayEpochDay(): Long = 100L
    override fun nowMillis(): Long = 5_000L
}

private class FakeQuestionRepository(var questions: List<Question> = emptyList()) : QuestionRepository {
    override suspend fun getAll(certificationId: String): List<Question> = questions
    override suspend fun getById(id: String): Question? = questions.firstOrNull { it.id == id }
    override suspend fun count(): Int = questions.size
    override suspend fun upsertAll(questions: List<Question>) {
        this.questions = questions
    }
}

private class FakeAttemptRepository : AttemptRepository {
    val recorded = mutableListOf<Attempt>()
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
    var state = StreakState()
    override fun observe(): Flow<StreakState> = flowOf(state)
    override suspend fun get(): StreakState = state
    override suspend fun set(state: StreakState) {
        this.state = state
    }
}
