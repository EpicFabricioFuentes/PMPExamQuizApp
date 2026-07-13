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
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PmpApplication : Application(), KoinComponent {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val bankImporter: BankImporter by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            // Verbose DI logging only in debug builds; silence it in release.
            androidLogger(if (BuildConfig.DEBUG) Level.INFO else Level.NONE)
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

        // Seed / update the bundled question bank into Room off the main thread. A failure here
        // (asset parse/validation, or a Room write error) must not crash the app: the study
        // screens surface their own empty/error states. Log it and report a non-fatal so the
        // failure is visible in Crashlytics rather than silently swallowed.
        applicationScope.launch {
            try {
                val imported = bankImporter.importIfNeeded()
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "Bank import complete: $imported question(s) imported")
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Question bank import failed", t)
                // Guarded: Crashlytics is inert unless google-services.json wired Firebase up.
                runCatching { FirebaseCrashlytics.getInstance().recordException(t) }
            }
        }
    }

    private companion object {
        const val TAG = "PmpApplication"
    }
}
