package com.fax.passyourpmpexam.feature.settings

import com.fax.passyourpmpexam.core.domain.model.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val reminderEnabled: Boolean = false,
    val reminderMinuteOfDay: Int = DEFAULT_REMINDER_MINUTE_OF_DAY,
) {
    companion object {
        const val DEFAULT_REMINDER_MINUTE_OF_DAY = 20 * 60 // 20:00
    }
}
