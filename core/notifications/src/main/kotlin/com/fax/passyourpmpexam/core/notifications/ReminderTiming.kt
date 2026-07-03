package com.fax.passyourpmpexam.core.notifications

/** Pure time math for the daily reminder — no Android types, so it's unit-testable. */
internal object ReminderTiming {

    const val MILLIS_PER_MINUTE: Long = 60_000L
    const val MILLIS_PER_DAY: Long = 24L * 60L * MILLIS_PER_MINUTE

    /**
     * Delay in millis from [nowMillisOfDay] (millis elapsed since local midnight) until the next
     * occurrence of [targetMinuteOfDay]. If the target is now or already passed today, returns the
     * delay until the same time tomorrow (never returns 0 or negative — avoids an immediate re-fire).
     */
    fun delayMillisUntilNext(nowMillisOfDay: Long, targetMinuteOfDay: Int): Long {
        val target = targetMinuteOfDay.coerceIn(0, 24 * 60 - 1) * MILLIS_PER_MINUTE
        val diff = target - nowMillisOfDay
        return if (diff > 0L) diff else diff + MILLIS_PER_DAY
    }
}
