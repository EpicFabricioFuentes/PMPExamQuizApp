package com.fax.passyourpmpexam.core.domain.scheduler

/**
 * Schedules (or cancels) the daily study reminder.
 *
 * Pure interface so feature/UI layers can depend on it without pulling in the
 * Android/WorkManager implementation. The concrete scheduler lives in
 * `:core:notifications`.
 */
interface ReminderScheduler {
    /** Schedule the next reminder to fire at [minuteOfDay] (minutes since local midnight, 0..1439). */
    fun schedule(minuteOfDay: Int)

    /** Cancel any pending reminder. */
    fun cancel()
}
