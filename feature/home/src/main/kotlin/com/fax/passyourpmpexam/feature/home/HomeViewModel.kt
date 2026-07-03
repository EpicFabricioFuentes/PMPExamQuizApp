package com.fax.passyourpmpexam.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fax.passyourpmpexam.core.common.TimeProvider
import com.fax.passyourpmpexam.core.domain.repository.SettingsRepository
import com.fax.passyourpmpexam.core.domain.repository.StreakRepository
import com.fax.passyourpmpexam.core.domain.streak.StreakCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val streakRepository: StreakRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    val state: StateFlow<HomeUiState> = streakRepository.observe()
        .map { streakState ->
            val today = timeProvider.todayEpochDay()
            HomeUiState(
                streakCount = StreakCalculator.displayStreak(streakState, today),
                dailyCompletedToday = settingsRepository.getDailyLastAnsweredEpochDay() == today,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HomeUiState(),
        )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
