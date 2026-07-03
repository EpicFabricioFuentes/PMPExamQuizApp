package com.fax.passyourpmpexam

import android.app.Application
import android.util.Log
import com.fax.passyourpmpexam.core.ads.adsModule
import com.fax.passyourpmpexam.core.data.content.BankImporter
import com.fax.passyourpmpexam.core.data.di.dataModule
import com.fax.passyourpmpexam.core.notifications.notificationsModule
import com.fax.passyourpmpexam.feature.daily.dailyModule
import com.fax.passyourpmpexam.feature.home.homeModule
import com.fax.passyourpmpexam.feature.free.freeModule
import com.fax.passyourpmpexam.feature.quiz.quizModule
import com.fax.passyourpmpexam.feature.settings.settingsModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

class PmpApplication : Application(), KoinComponent {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val bankImporter: BankImporter by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@PmpApplication)
            modules(
                dataModule,
                notificationsModule,
                adsModule,
                homeModule,
                dailyModule,
                quizModule,
                freeModule,
                settingsModule,
            )
        }
        // The Mobile Ads SDK is initialized by ConsentManager once UMP consent permits ad requests
        // (see MainActivity#onCreate), so it is not started here unconditionally.

        // Seed / update the bundled question bank into Room off the main thread.
        applicationScope.launch {
            val imported = bankImporter.importIfNeeded()
            Log.i(TAG, "Bank import complete: $imported question(s) imported")
        }
    }

    private companion object {
        const val TAG = "PmpApplication"
    }
}
