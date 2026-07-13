package com.fax.passyourpmpexam.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fax.passyourpmpexam.core.common.TimeProvider
import com.fax.passyourpmpexam.core.domain.repository.AttemptRepository
import com.fax.passyourpmpexam.core.domain.repository.QuestionRepository
import com.fax.passyourpmpexam.core.domain.repository.SettingsRepository
import com.fax.passyourpmpexam.core.domain.repository.StreakRepository
import com.fax.passyourpmpexam.core.domain.streak.StreakCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

class HomeViewModel(
    private val streakRepository: StreakRepository,
    private val settingsRepository: SettingsRepository,
    private val questionRepository: QuestionRepository,
    private val attemptRepository: AttemptRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val today = timeProvider.todayEpochDay()

    // Re-subscribes the data streams when the user retries after an error.
    private val retryTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<HomeUiState> = retryTrigger
        .flatMapLatest {
            combine(
                streakRepository.observe(),
                settingsRepository.observeDailyGoal(),
                attemptRepository.observeAnsweredCountBetween(startOfTodayMillis(), startOfTomorrowMillis()),
            ) { streakState, dailyGoal, answeredToday ->
                HomeUiState(
                    streakCount = StreakCalculator.displayStreak(streakState, today),
                    answeredToday = answeredToday,
                    dailyGoal = dailyGoal,
                    questionCount = questionRepository.count(),
                )
            }.catch { emit(HomeUiState(error = LOAD_ERROR_MESSAGE)) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HomeUiState(),
        )

    /** Re-subscribe the hub's data streams after an error. */
    fun onRetry() {
        retryTrigger.value += 1
    }

    private fun startOfTodayMillis(): Long = epochDayStartMillis(today)

    private fun startOfTomorrowMillis(): Long = epochDayStartMillis(today + 1)

    private fun epochDayStartMillis(epochDay: Long): Long =
        LocalDate.ofEpochDay(epochDay).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val LOAD_ERROR_MESSAGE = "We couldn't load your progress. Please try again."
    }
}
