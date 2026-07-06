package com.fax.passyourpmpexam.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.fax.passyourpmpexam.core.data.datastore.PmpPreferencesKeys
import com.fax.passyourpmpexam.core.domain.model.DailyGoal
import com.fax.passyourpmpexam.core.domain.model.ThemeMode
import com.fax.passyourpmpexam.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override fun observeThemeMode(): Flow<ThemeMode> =
        dataStore.data.map { ThemeMode.fromStorage(it[PmpPreferencesKeys.THEME_MODE]) }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[PmpPreferencesKeys.THEME_MODE] = mode.name }
    }

    override fun observeReminderEnabled(): Flow<Boolean> =
        dataStore.data.map { it[PmpPreferencesKeys.REMINDER_ENABLED] ?: false }

    override suspend fun setReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[PmpPreferencesKeys.REMINDER_ENABLED] = enabled }
    }

    override fun observeReminderMinuteOfDay(): Flow<Int> =
        dataStore.data.map { it[PmpPreferencesKeys.REMINDER_MINUTE_OF_DAY] ?: DEFAULT_REMINDER_MINUTE_OF_DAY }

    override suspend fun setReminderMinuteOfDay(minuteOfDay: Int) {
        dataStore.edit { it[PmpPreferencesKeys.REMINDER_MINUTE_OF_DAY] = minuteOfDay }
    }

    override fun observeDailyGoal(): Flow<Int> =
        dataStore.data.map { DailyGoal.coerce(it[PmpPreferencesKeys.DAILY_GOAL] ?: DailyGoal.DEFAULT) }

    override suspend fun setDailyGoal(goal: Int) {
        dataStore.edit { it[PmpPreferencesKeys.DAILY_GOAL] = DailyGoal.coerce(goal) }
    }

    override suspend fun getInstalledBankVersion(): Int =
        dataStore.data.map { it[PmpPreferencesKeys.INSTALLED_BANK_VERSION] ?: 0 }.first()

    override suspend fun setInstalledBankVersion(version: Int) {
        dataStore.edit { it[PmpPreferencesKeys.INSTALLED_BANK_VERSION] = version }
    }

    override fun observeHasCompletedFirstRun(): Flow<Boolean> =
        dataStore.data.map { it[PmpPreferencesKeys.HAS_COMPLETED_FIRST_RUN] ?: false }

    override suspend fun setFirstRunCompleted() {
        dataStore.edit { it[PmpPreferencesKeys.HAS_COMPLETED_FIRST_RUN] = true }
    }

    override suspend fun getDailyLastAnsweredEpochDay(): Long =
        dataStore.data.map { it[PmpPreferencesKeys.DAILY_LAST_ANSWERED_EPOCH_DAY] ?: NO_DAY }.first()

    override suspend fun setDailyLastAnsweredEpochDay(epochDay: Long) {
        dataStore.edit { it[PmpPreferencesKeys.DAILY_LAST_ANSWERED_EPOCH_DAY] = epochDay }
    }

    private companion object {
        /** 20:00, a sensible default study-reminder time. */
        const val DEFAULT_REMINDER_MINUTE_OF_DAY = 20 * 60
        const val NO_DAY = -1L
    }
}
