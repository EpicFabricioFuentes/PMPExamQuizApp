package com.fax.passyourpmpexam.core.domain.model

/**
 * The user's daily study target: how many questions they aim to answer per day, counted across all
 * modes. Single source of truth for the allowed range and default so persistence, Settings, and Home
 * never drift.
 */
object DailyGoal {
    const val MIN = 1
    const val MAX = 20
    const val DEFAULT = 1

    fun coerce(value: Int): Int = value.coerceIn(MIN, MAX)
}
