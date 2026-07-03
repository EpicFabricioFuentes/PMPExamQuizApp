package com.fax.passyourpmpexam.feature.daily

import com.fax.passyourpmpexam.core.common.IdGenerator
import com.fax.passyourpmpexam.core.common.TimeProvider
import com.fax.passyourpmpexam.core.domain.model.Attempt
import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.Question
import com.fax.passyourpmpexam.core.domain.model.StreakState
import com.fax.passyourpmpexam.core.domain.model.ThemeMode
import com.fax.passyourpmpexam.core.domain.repository.AttemptRepository
import com.fax.passyourpmpexam.core.domain.repository.QuestionRepository
import com.fax.passyourpmpexam.core.domain.repository.SettingsRepository
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DailyViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var questions: FakeQuestionRepository
    private lateinit var attempts: FakeAttemptRepository
    private lateinit var streak: FakeStreakRepository
    private lateinit var settings: FakeSettingsRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        questions = FakeQuestionRepository()
        attempts = FakeAttemptRepository()
        streak = FakeStreakRepository()
        settings = FakeSettingsRepository()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = DailyViewModel(
        questionRepository = questions,
        attemptRepository = attempts,
        streakRepository = streak,
        settingsRepository = settings,
        timeProvider = FakeTimeProvider(today = TODAY, millis = NOW),
        idGenerator = IdGenerator { ATTEMPT_ID },
    )

    @Test
    fun loadsDeterministicDailyQuestion() = runTest(dispatcher) {
        // Pool sorts by id: q0,q1,q2; TODAY(100) % 3 == 1 -> q1.
        questions.questions = listOf(question("q0"), question("q1"), question("q2"))

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is DailyUiState.Ready)
        assertEquals("q1", state.question.id)
        assertEquals(false, state.answered)
    }

    @Test
    fun emptyPoolEmitsEmpty() = runTest(dispatcher) {
        questions.questions = emptyList()

        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(DailyUiState.Empty, vm.state.value)
    }

    @Test
    fun answeringCorrectlyRecordsAttemptUpdatesStreakAndMarksDay() = runTest(dispatcher) {
        questions.questions = listOf(question("only", correctIndex = 2))

        val vm = viewModel()
        advanceUntilIdle()

        vm.onIntent(DailyIntent.SelectOption(2))
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is DailyUiState.Ready)
        assertEquals(true, state.answered)
        assertEquals(true, state.isCorrect)

        assertEquals(1, attempts.recorded.size)
        val recorded = attempts.recorded.single()
        assertEquals("only", recorded.questionId)
        assertEquals(true, recorded.isCorrect)
        assertEquals(ATTEMPT_ID, recorded.id)

        assertEquals(1, streak.state.currentStreak)
        assertEquals(TODAY, settings.dailyLastAnswered)
    }

    @Test
    fun alreadyCompletedTodayLoadsAsAnswered() = runTest(dispatcher) {
        questions.questions = listOf(question("only"))
        settings.dailyLastAnswered = TODAY

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is DailyUiState.Ready)
        assertTrue(state.answered)
        assertTrue(state.alreadyCompletedToday)
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

    private companion object {
        const val TODAY = 100L
        const val NOW = 5_000L
        const val ATTEMPT_ID = "attempt-id"
    }
}

private class FakeTimeProvider(private val today: Long, private val millis: Long) : TimeProvider {
    override fun todayEpochDay(): Long = today
    override fun nowMillis(): Long = millis
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
}

private class FakeStreakRepository : StreakRepository {
    var state = StreakState()
    override fun observe(): Flow<StreakState> = flowOf(state)
    override suspend fun get(): StreakState = state
    override suspend fun set(state: StreakState) {
        this.state = state
    }
}

private class FakeSettingsRepository : SettingsRepository {
    var dailyLastAnswered = -1L
    var installedBankVersion = 0

    override fun observeThemeMode(): Flow<ThemeMode> = flowOf(ThemeMode.SYSTEM)
    override suspend fun setThemeMode(mode: ThemeMode) = Unit
    override fun observeReminderEnabled(): Flow<Boolean> = flowOf(false)
    override suspend fun setReminderEnabled(enabled: Boolean) = Unit
    override fun observeReminderMinuteOfDay(): Flow<Int> = flowOf(0)
    override suspend fun setReminderMinuteOfDay(minuteOfDay: Int) = Unit
    override suspend fun getInstalledBankVersion(): Int = installedBankVersion
    override suspend fun setInstalledBankVersion(version: Int) {
        installedBankVersion = version
    }
    override suspend fun getDailyLastAnsweredEpochDay(): Long = dailyLastAnswered
    override suspend fun setDailyLastAnsweredEpochDay(epochDay: Long) {
        dailyLastAnswered = epochDay
    }
}
