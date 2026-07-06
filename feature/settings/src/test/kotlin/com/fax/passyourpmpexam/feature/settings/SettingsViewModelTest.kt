package com.fax.passyourpmpexam.feature.settings

import com.fax.passyourpmpexam.core.domain.model.DailyGoal
import com.fax.passyourpmpexam.core.domain.model.ThemeMode
import com.fax.passyourpmpexam.core.domain.repository.SettingsRepository
import com.fax.passyourpmpexam.core.domain.scheduler.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var settings: FakeSettingsRepository
    private lateinit var scheduler: FakeReminderScheduler

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        settings = FakeSettingsRepository()
        scheduler = FakeReminderScheduler()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun intentsPropagateToRepository() = runTest(dispatcher) {
        val vm = SettingsViewModel(settings, scheduler)

        vm.onIntent(SettingsIntent.SetTheme(ThemeMode.DARK))
        vm.onIntent(SettingsIntent.SetReminderEnabled(true))
        vm.onIntent(SettingsIntent.SetReminderTime(480))
        advanceUntilIdle()

        assertEquals(ThemeMode.DARK, settings.themeFlow.value)
        assertTrue(settings.enabledFlow.value)
        assertEquals(480, settings.minuteFlow.value)
    }

    @Test
    fun stateReflectsRepositoryValues() = runTest(dispatcher) {
        settings.themeFlow.value = ThemeMode.LIGHT
        settings.enabledFlow.value = true
        settings.minuteFlow.value = 480

        val vm = SettingsViewModel(settings, scheduler)
        val collector = launch { vm.state.collect {} } // activate WhileSubscribed
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(ThemeMode.LIGHT, state.themeMode)
        assertTrue(state.reminderEnabled)
        assertEquals(480, state.reminderMinuteOfDay)

        collector.cancel()
    }

    @Test
    fun enablingReminderSchedulesAtCurrentTime() = runTest(dispatcher) {
        settings.minuteFlow.value = 540 // 09:00
        val vm = SettingsViewModel(settings, scheduler)
        val collector = launch { vm.state.collect {} } // keep state.value live
        advanceUntilIdle()

        vm.onIntent(SettingsIntent.SetReminderEnabled(true))
        advanceUntilIdle()

        assertEquals(listOf(540), scheduler.scheduled)
        assertEquals(0, scheduler.cancelCount)
        collector.cancel()
    }

    @Test
    fun disablingReminderCancels() = runTest(dispatcher) {
        settings.enabledFlow.value = true
        val vm = SettingsViewModel(settings, scheduler)
        val collector = launch { vm.state.collect {} }
        advanceUntilIdle()

        vm.onIntent(SettingsIntent.SetReminderEnabled(false))
        advanceUntilIdle()

        assertEquals(1, scheduler.cancelCount)
        assertTrue(scheduler.scheduled.isEmpty())
        collector.cancel()
    }

    @Test
    fun changingTimeWhileEnabledReschedules() = runTest(dispatcher) {
        settings.enabledFlow.value = true
        val vm = SettingsViewModel(settings, scheduler)
        val collector = launch { vm.state.collect {} }
        advanceUntilIdle()

        vm.onIntent(SettingsIntent.SetReminderTime(600))
        advanceUntilIdle()

        assertEquals(listOf(600), scheduler.scheduled)
        collector.cancel()
    }

    @Test
    fun settingDailyGoalPersistsAndCoercesToRange() = runTest(dispatcher) {
        val vm = SettingsViewModel(settings, scheduler)
        val collector = launch { vm.state.collect {} }

        vm.onIntent(SettingsIntent.SetDailyGoal(5))
        advanceUntilIdle()
        assertEquals(5, settings.dailyGoalFlow.value)
        assertEquals(5, vm.state.value.dailyGoal)

        vm.onIntent(SettingsIntent.SetDailyGoal(DailyGoal.MAX + 100))
        advanceUntilIdle()
        assertEquals(DailyGoal.MAX, settings.dailyGoalFlow.value)

        collector.cancel()
    }

    @Test
    fun changingTimeWhileDisabledDoesNotSchedule() = runTest(dispatcher) {
        settings.enabledFlow.value = false
        val vm = SettingsViewModel(settings, scheduler)
        val collector = launch { vm.state.collect {} }
        advanceUntilIdle()

        vm.onIntent(SettingsIntent.SetReminderTime(600))
        advanceUntilIdle()

        assertTrue(scheduler.scheduled.isEmpty())
        collector.cancel()
    }
}

private class FakeReminderScheduler : ReminderScheduler {
    val scheduled = mutableListOf<Int>()
    var cancelCount = 0
    override fun schedule(minuteOfDay: Int) {
        scheduled += minuteOfDay
    }

    override fun cancel() {
        cancelCount++
    }
}

private class FakeSettingsRepository : SettingsRepository {
    val themeFlow = MutableStateFlow(ThemeMode.SYSTEM)
    val enabledFlow = MutableStateFlow(false)
    val minuteFlow = MutableStateFlow(20 * 60)
    val dailyGoalFlow = MutableStateFlow(DailyGoal.DEFAULT)

    override fun observeThemeMode(): Flow<ThemeMode> = themeFlow
    override suspend fun setThemeMode(mode: ThemeMode) {
        themeFlow.value = mode
    }

    override fun observeReminderEnabled(): Flow<Boolean> = enabledFlow
    override suspend fun setReminderEnabled(enabled: Boolean) {
        enabledFlow.value = enabled
    }

    override fun observeReminderMinuteOfDay(): Flow<Int> = minuteFlow
    override suspend fun setReminderMinuteOfDay(minuteOfDay: Int) {
        minuteFlow.value = minuteOfDay
    }

    override fun observeDailyGoal(): Flow<Int> = dailyGoalFlow
    override suspend fun setDailyGoal(goal: Int) {
        dailyGoalFlow.value = goal
    }

    override suspend fun getInstalledBankVersion(): Int = 0
    override suspend fun setInstalledBankVersion(version: Int) = Unit
    override fun observeHasCompletedFirstRun(): Flow<Boolean> = MutableStateFlow(true)
    override suspend fun setFirstRunCompleted() = Unit
    override suspend fun getDailyLastAnsweredEpochDay(): Long = -1L
    override suspend fun setDailyLastAnsweredEpochDay(epochDay: Long) = Unit
}
