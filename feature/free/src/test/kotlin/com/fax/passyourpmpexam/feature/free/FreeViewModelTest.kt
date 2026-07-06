package com.fax.passyourpmpexam.feature.free

import com.fax.passyourpmpexam.core.common.IdGenerator
import com.fax.passyourpmpexam.core.common.TimeProvider
import com.fax.passyourpmpexam.core.domain.model.Attempt
import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.Question
import com.fax.passyourpmpexam.core.domain.model.QuizMode
import com.fax.passyourpmpexam.core.domain.model.StreakState
import com.fax.passyourpmpexam.core.domain.repository.AttemptRepository
import com.fax.passyourpmpexam.core.domain.repository.QuestionRepository
import com.fax.passyourpmpexam.core.domain.repository.StreakRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FreeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var questions: FakeQuestionRepository
    private lateinit var attempts: FakeAttemptRepository
    private lateinit var streak: FakeStreakRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        questions = FakeQuestionRepository()
        attempts = FakeAttemptRepository()
        streak = FakeStreakRepository()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = FreeViewModel(
        questionRepository = questions,
        attemptRepository = attempts,
        streakRepository = streak,
        timeProvider = FakeTimeProvider,
        idGenerator = IdGenerator { "id" },
        random = Random(0),
    )

    @Test
    fun togglingDomainFiltersQuestions() = runTest(dispatcher) {
        questions.questions = listOf(
            question("people", Domain.PEOPLE),
            question("process", Domain.PROCESS),
        )

        val vm = viewModel()
        advanceUntilIdle()

        vm.onIntent(FreeIntent.ToggleDomain(Domain.PEOPLE))

        val state = vm.state.value
        assertEquals(setOf(Domain.PEOPLE), state.selectedDomains)
        assertEquals(Domain.PEOPLE, state.question?.domain)
    }

    @Test
    fun answeringRecordsAttemptUpdatesStreakThenAdvances() = runTest(dispatcher) {
        questions.questions = listOf(question("only", Domain.PROCESS, correctIndex = 1))

        val vm = viewModel()
        advanceUntilIdle()

        vm.onIntent(FreeIntent.SelectOption(1))
        advanceUntilIdle()

        val answered = vm.state.value
        assertTrue(answered.answered)
        assertEquals(true, answered.isCorrect)
        assertEquals(1, attempts.recorded.size)
        assertEquals(QuizMode.FREE, attempts.recorded.single().mode)
        assertEquals(1, streak.state.currentStreak)

        vm.onIntent(FreeIntent.Next)
        val next = vm.state.value
        assertFalse(next.answered)
        assertEquals(null, next.selectedIndex)
        assertTrue(next.question != null) // unlimited: reshuffles when the queue empties
    }

    private fun question(id: String, domain: Domain, correctIndex: Int = 0) = Question(
        id = id,
        certificationId = "PMP",
        domain = domain,
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

private class FakeStreakRepository : StreakRepository {
    var state = StreakState()
    override fun observe(): Flow<StreakState> = flowOf(state)
    override suspend fun get(): StreakState = state
    override suspend fun set(state: StreakState) {
        this.state = state
    }
}
