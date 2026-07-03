package com.fax.passyourpmpexam.core.domain.streak

import com.fax.passyourpmpexam.core.domain.model.StreakState

/**
 * Pure streak logic. Per the design spec, "activity" is any study action that day
 * (at least one question answered in any mode).
 *
 * Rules:
 *  - First-ever activity starts the streak at 1.
 *  - A second activity on the same day does not increment.
 *  - Activity on the day immediately after the last activity increments by 1.
 *  - Activity after a gap of 2+ days resets the streak to 1.
 *  - Events dated before the last recorded activity are ignored (clock skew guard).
 */
object StreakCalculator {

    /** Returns the new [StreakState] after registering activity on [todayEpochDay]. */
    fun onActivity(state: StreakState, todayEpochDay: Long): StreakState {
        val last = state.lastActivityEpochDay
        val newCurrent = when {
            last == StreakState.NO_ACTIVITY -> 1
            todayEpochDay == last -> state.currentStreak
            todayEpochDay == last + 1 -> state.currentStreak + 1
            todayEpochDay > last + 1 -> 1
            else -> state.currentStreak // past-dated event: leave streak untouched
        }
        return state.copy(
            currentStreak = newCurrent,
            longestStreak = maxOf(state.longestStreak, newCurrent),
            lastActivityEpochDay = maxOf(last, todayEpochDay),
        )
    }

    /**
     * The streak to show on [todayEpochDay]. The stored [StreakState.currentStreak] is only
     * still "alive" if the last activity was today or yesterday; otherwise it has lapsed to 0.
     */
    fun displayStreak(state: StreakState, todayEpochDay: Long): Int {
        if (state.lastActivityEpochDay == StreakState.NO_ACTIVITY) return 0
        val gap = todayEpochDay - state.lastActivityEpochDay
        return if (gap <= 1L) state.currentStreak else 0
    }
}
