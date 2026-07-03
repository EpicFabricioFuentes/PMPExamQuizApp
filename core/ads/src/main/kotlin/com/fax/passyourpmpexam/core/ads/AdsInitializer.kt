package com.fax.passyourpmpexam.core.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds

/** Initializes the Mobile Ads SDK. Call once from Application.onCreate; the SDK is idempotent. */
object AdsInitializer {
    fun initialize(context: Context) {
        MobileAds.initialize(context) { /* initialization complete */ }
    }
}
