package com.fax.passyourpmpexam.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fax.passyourpmpexam.core.domain.model.DailyGoal
import com.fax.passyourpmpexam.core.domain.repository.SettingsRepository
import com.fax.passyourpmpexam.core.domain.scheduler.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    val state: StateFlow<SettingsUiState> = combine(
        settingsRepository.observeThemeMode(),
        settingsRepository.observeReminderEnabled(),
        settingsRepository.observeReminderMinuteOfDay(),
        settingsRepository.observeDailyGoal(),
    ) { theme, reminderEnabled, reminderMinute, dailyGoal ->
        SettingsUiState(
            themeMode = theme,
            reminderEnabled = reminderEnabled,
            reminderMinuteOfDay = reminderMinute,
            dailyGoal = dailyGoal,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = SettingsUiState(),
    )

    fun onIntent(intent: SettingsIntent) {
        viewModelScope.launch {
            when (intent) {
                is SettingsIntent.SetTheme -> settingsRepository.setThemeMode(intent.mode)

                is SettingsIntent.SetReminderEnabled -> {
                    settingsRepository.setReminderEnabled(intent.enabled)
                    if (intent.enabled) {
                        reminderScheduler.schedule(state.value.reminderMinuteOfDay)
                    } else {
                        reminderScheduler.cancel()
                    }
                }

                is SettingsIntent.SetReminderTime -> {
                    settingsRepository.setReminderMinuteOfDay(intent.minuteOfDay)
                    if (state.value.reminderEnabled) {
                        reminderScheduler.schedule(intent.minuteOfDay)
                    }
                }

                is SettingsIntent.SetDailyGoal ->
                    settingsRepository.setDailyGoal(DailyGoal.coerce(intent.goal))
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
