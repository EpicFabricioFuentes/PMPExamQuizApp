package com.fax.passyourpmpexam.feature.settings

import com.fax.passyourpmpexam.core.domain.model.ThemeMode

sealed interface SettingsIntent {
    data class SetTheme(val mode: ThemeMode) : SettingsIntent
    data class SetReminderEnabled(val enabled: Boolean) : SettingsIntent
    data class SetReminderTime(val minuteOfDay: Int) : SettingsIntent
}
