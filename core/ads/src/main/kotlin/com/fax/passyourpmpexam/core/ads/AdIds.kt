package com.fax.passyourpmpexam.core.ads

/**
 * Ad unit IDs. Sourced from [BuildConfig], which the module's build.gradle.kts populates from
 * `secrets.properties` (falling back to Google's public TEST banner unit — always safe to load and
 * never bills). Set ADMOB_BANNER_UNIT_ID + ADMOB_APP_ID in secrets.properties before release.
 */
object AdIds {
    val BANNER_UNIT_ID: String = BuildConfig.ADMOB_BANNER_UNIT_ID
}
