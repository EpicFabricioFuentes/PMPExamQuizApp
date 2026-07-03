package com.fax.passyourpmpexam.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fax.passyourpmpexam.core.domain.repository.SettingsRepository
import com.fax.passyourpmpexam.core.domain.scheduler.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Re-schedules the reminder after a reboot (WorkManager schedules are cleared on boot). */
class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val settingsRepository: SettingsRepository by inject()
    private val scheduler: ReminderScheduler by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                if (settingsRepository.observeReminderEnabled().first()) {
                    val minuteOfDay = settingsRepository.observeReminderMinuteOfDay().first()
                    scheduler.schedule(minuteOfDay)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
