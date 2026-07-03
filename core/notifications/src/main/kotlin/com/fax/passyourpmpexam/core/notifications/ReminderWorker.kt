package com.fax.passyourpmpexam.core.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fax.passyourpmpexam.core.domain.scheduler.ReminderScheduler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Posts the daily reminder, then re-schedules the next one so the unique-work chain repeats daily.
 * Instantiated by the default WorkManager factory; dependencies come from Koin.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    private val notifier: ReminderNotifier by inject()
    private val scheduler: ReminderScheduler by inject()

    override suspend fun doWork(): Result {
        notifier.postDailyReminder()
        val minuteOfDay = inputData.getInt(KEY_MINUTE_OF_DAY, DEFAULT_MINUTE_OF_DAY)
        scheduler.schedule(minuteOfDay)
        return Result.success()
    }

    companion object {
        const val KEY_MINUTE_OF_DAY: String = "minute_of_day"
        private const val DEFAULT_MINUTE_OF_DAY: Int = 20 * 60
    }
}
