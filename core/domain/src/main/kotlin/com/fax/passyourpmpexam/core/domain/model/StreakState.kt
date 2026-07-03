package com.fax.passyourpmpexam.core.domain.model

/**
 * Persisted streak state. [lastActivityEpochDay] is the epoch day (days since 1970-01-01)
 * of the most recent study activity, or [NO_ACTIVITY] if the user has never studied.
 */
data class StreakState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActivityEpochDay: Long = NO_ACTIVITY,
) {
    companion object {
        const val NO_ACTIVITY: Long = -1L
    }
}
