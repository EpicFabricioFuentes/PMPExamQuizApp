package com.fax.passyourpmpexam.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.fax.passyourpmpexam.core.data.datastore.PmpPreferencesKeys
import com.fax.passyourpmpexam.core.domain.model.StreakState
import com.fax.passyourpmpexam.core.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class StreakRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : StreakRepository {

    override fun observe(): Flow<StreakState> = dataStore.data.map { it.toStreakState() }

    override suspend fun get(): StreakState = observe().first()

    override suspend fun set(state: StreakState) {
        dataStore.edit {
            it[PmpPreferencesKeys.CURRENT_STREAK] = state.currentStreak
            it[PmpPreferencesKeys.LONGEST_STREAK] = state.longestStreak
            it[PmpPreferencesKeys.LAST_ACTIVITY_EPOCH_DAY] = state.lastActivityEpochDay
        }
    }

    private fun Preferences.toStreakState(): StreakState =
        StreakState(
            currentStreak = this[PmpPreferencesKeys.CURRENT_STREAK] ?: 0,
            longestStreak = this[PmpPreferencesKeys.LONGEST_STREAK] ?: 0,
            lastActivityEpochDay = this[PmpPreferencesKeys.LAST_ACTIVITY_EPOCH_DAY] ?: StreakState.NO_ACTIVITY,
        )
}
