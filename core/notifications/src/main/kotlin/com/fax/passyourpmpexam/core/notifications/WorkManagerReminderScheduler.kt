package com.fax.passyourpmpexam.core.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.fax.passyourpmpexam.core.domain.scheduler.ReminderScheduler
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Schedules the reminder as a unique one-shot [ReminderWorker] delayed to the next occurrence of the
 * chosen time. The worker re-enqueues itself after firing, so a single unique-work chain repeats daily.
 */
class WorkManagerReminderScheduler(
    private val context: Context,
) : ReminderScheduler {

    override fun schedule(minuteOfDay: Int) {
        val now = LocalTime.now()
        val nowMillisOfDay = now.toSecondOfDay() * 1_000L
        val delayMillis = ReminderTiming.delayMillisUntilNext(nowMillisOfDay, minuteOfDay)

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_MINUTE_OF_DAY to minuteOfDay))
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    override fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    companion object {
        const val WORK_NAME: String = "pmp_daily_reminder"
    }
}
