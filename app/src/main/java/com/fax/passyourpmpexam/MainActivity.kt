package com.fax.passyourpmpexam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.fax.passyourpmpexam.core.ads.ConsentManager
import com.fax.passyourpmpexam.core.designsystem.theme.PmpTheme
import com.fax.passyourpmpexam.core.domain.model.ThemeMode
import com.fax.passyourpmpexam.core.domain.repository.SettingsRepository
import com.fax.passyourpmpexam.ui.PmpApp
import com.fax.passyourpmpexam.ui.WelcomeScreen
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {

    private val consentManager: ConsentManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Request UMP consent; the Ads SDK initializes and the Home banner loads only once allowed.
        consentManager.gatherConsent(this)
        setContent {
            val settingsRepository = koinInject<SettingsRepository>()
            val themeMode by settingsRepository.observeThemeMode()
                .collectAsState(initial = ThemeMode.SYSTEM)
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            PmpTheme(darkTheme = darkTheme) {
                // Gate on the first-run flag. `null` = DataStore read pending, so we render nothing
                // briefly rather than flash the welcome screen at returning users.
                val hasCompletedFirstRun by settingsRepository.observeHasCompletedFirstRun()
                    .collectAsState(initial = null)
                val scope = rememberCoroutineScope()
                when (hasCompletedFirstRun) {
                    null -> Unit
                    false -> WelcomeScreen(
                        onGetStarted = { scope.launch { settingsRepository.setFirstRunCompleted() } },
                    )
                    true -> PmpApp()
                }
            }
        }
    }
}
