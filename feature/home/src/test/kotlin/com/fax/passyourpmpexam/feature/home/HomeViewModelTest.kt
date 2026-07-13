package com.fax.passyourpmpexam.feature.home

import com.fax.passyourpmpexam.core.common.TimeProvider
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var streak: FakeStreakRepository
    private lateinit var settings: FakeSettingsRepository
    private lateinit var questions: FakeQuestionRepository
    private lateinit var attempts: FakeAttemptRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        streak = FakeStreakRepository()
        settings = FakeSettingsRepository()
        questions = FakeQuestionRepository()
        attempts = FakeAttemptRepository()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = HomeViewModel(
        streakRepository = streak,
        settingsRepository = settings,
        questionRepository = questions,
        attemptRepository = attempts,
        timeProvider = FakeTimeProvider,
    )

    @Test
    fun combinesRepositoriesIntoState() = runTest(dispatcher) {
        questions.questions = List(5) { question("q$it") }
        // Last activity == today (epochDay 100) so the streak is still "alive".
        streak.state.value = StreakState(currentStreak = 3, longestStreak = 4, lastActivityEpochDay = 100L)
        settings.dailyGoal.value = 7
        attempts.answeredToday.value = 2

        val vm = viewModel()
        val job = launch { vm.state.collect {} } // WhileSubscribed: needs a subscriber to run
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(3, state.streakCount)
        assertEquals(2, state.answeredToday)
        assertEquals(7, state.dailyGoal)
        assertEquals(5, state.questionCount)

        job.cancel()
    }

    @Test
    fun lapsedStreakDisplaysZero() = runTest(dispatcher) {
        // Last activity 10 days ago → the current streak has lapsed and should display as 0.
        streak.state.value = StreakState(currentStreak = 5, longestStreak = 5, lastActivityEpochDay = 90L)

        val vm = viewModel()
        val job = launch { vm.state.collect {} }
        advanceUntilIdle()

        assertEquals(0, vm.state.value.streakCount)

        job.cancel()
    }

    private fun question(id: String, domain: Domain = Domain.PROCESS) = Question(
        id = id,
        certificationId = "PMP",
        domain = domain,
        text = "text",
        options = listOf("a", "b", "c", "d"),
        correctIndex = 0,
        explanation = "explanation",
        bankVersion = 1,
    )
}

private object FakeTimeProvider : TimeProvider {
    override fun todayEpochDay(): Long = 100L
    override fun nowMillis(): Long = 5_000L
}

private class FakeStreakRepository : StreakRepository {
    val state = MutableStateFlow(StreakState())
    override fun observe(): Flow<StreakState> = state
    override suspend fun get(): StreakState = state.value
    override suspend fun set(state: StreakState) {
        this.state.value = state
    }
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
    val answeredToday = MutableStateFlow(0)
    override suspend fun record(attempt: Attempt) = Unit
    override suspend fun recordAll(attempts: List<Attempt>) = Unit
    override fun observeAll(): Flow<List<Attempt>> = flowOf(emptyList())
    override suspend fun totalCount(): Int = 0
    override suspend fun correctCount(): Int = 0
    override fun observeAnsweredCountBetween(startMillis: Long, endMillis: Long): Flow<Int> = answeredToday
}

private class FakeSettingsRepository : SettingsRepository {
    val dailyGoal = MutableStateFlow(DailyGoal.DEFAULT)
    override fun observeThemeMode(): Flow<ThemeMode> = flowOf(ThemeMode.SYSTEM)
    override suspend fun setThemeMode(mode: ThemeMode) = Unit
    override fun observeReminderEnabled(): Flow<Boolean> = flowOf(false)
    override suspend fun setReminderEnabled(enabled: Boolean) = Unit
    override fun observeReminderMinuteOfDay(): Flow<Int> = flowOf(0)
    override suspend fun setReminderMinuteOfDay(minuteOfDay: Int) = Unit
    override fun observeDailyGoal(): Flow<Int> = dailyGoal
    override suspend fun setDailyGoal(goal: Int) {
        dailyGoal.value = goal
    }
    override suspend fun getInstalledBankVersion(): Int = 0
    override suspend fun setInstalledBankVersion(version: Int) = Unit
    override fun observeHasCompletedFirstRun(): Flow<Boolean> = flowOf(true)
    override suspend fun setFirstRunCompleted() = Unit
    override suspend fun getDailyLastAnsweredEpochDay(): Long = 0L
    override suspend fun setDailyLastAnsweredEpochDay(epochDay: Long) = Unit
}
