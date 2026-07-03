package com.fax.passyourpmpexam.core.notifications

import com.fax.passyourpmpexam.core.domain.scheduler.ReminderScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val notificationsModule: Module = module {
    single { ReminderNotifier(androidContext()) }
    single<ReminderScheduler> { WorkManagerReminderScheduler(androidContext()) }
}
