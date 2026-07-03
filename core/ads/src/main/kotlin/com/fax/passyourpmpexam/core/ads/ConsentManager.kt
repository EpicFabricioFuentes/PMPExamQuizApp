package com.fax.passyourpmpexam.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Wraps the User Messaging Platform (UMP) consent flow. On launch it refreshes consent info and,
 * where required (e.g. EEA/UK), shows the consent form. The Mobile Ads SDK is initialized only once
 * consent permits ad requests, and [canRequestAds] gates whether the Home banner may load.
 *
 * Outside consent regions UMP reports [ConsentInformation.canRequestAds] == true immediately, so
 * the banner loads without ever showing a form.
 */
class ConsentManager(private val appContext: Context) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(appContext)

    private val _canRequestAds = MutableStateFlow(consentInformation.canRequestAds())
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    /**
     * Call from an [Activity] on launch. Requests a consent-info update, shows the form if required,
     * then reflects the resulting consent state (initializing the Ads SDK on the first allow).
     * Safe to call more than once; UMP caches the consent status across sessions.
     */
    fun gatherConsent(activity: Activity) {
        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // Info updated: show the form if UMP says one is required, then re-check consent.
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { refresh() }
            },
            {
                // Update failed (e.g. offline): fall back to any consent persisted from a prior run.
                refresh()
            },
        )
        // Reflect consent already persisted from a previous session without waiting for the callback.
        refresh()
    }

    private fun refresh() {
        val allowed = consentInformation.canRequestAds()
        if (allowed && !_canRequestAds.value) {
            AdsInitializer.initialize(appContext)
        }
        _canRequestAds.value = allowed
    }
}
