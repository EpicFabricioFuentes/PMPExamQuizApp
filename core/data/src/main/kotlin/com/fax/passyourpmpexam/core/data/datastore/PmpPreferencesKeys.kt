package com.fax.passyourpmpexam.core.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/** DataStore keys for settings + streak scalars (spec §3.2). Read/written by repository impls in a later increment. */
object PmpPreferencesKeys {
    val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
    val REMINDER_MINUTE_OF_DAY = intPreferencesKey("reminder_minute_of_day")
    val CURRENT_STREAK = intPreferencesKey("current_streak")
    val LONGEST_STREAK = intPreferencesKey("longest_streak")
    val LAST_ACTIVITY_EPOCH_DAY = longPreferencesKey("last_activity_epoch_day")
    val DAILY_LAST_ANSWERED_EPOCH_DAY = longPreferencesKey("daily_last_answered_epoch_day")
    val DAILY_QUESTION_ID = stringPreferencesKey("daily_question_id")
    val INSTALLED_BANK_VERSION = intPreferencesKey("installed_bank_version")
    val HAS_COMPLETED_FIRST_RUN = booleanPreferencesKey("has_completed_first_run")
    val ANALYTICS_CONSENT = booleanPreferencesKey("analytics_consent")
    val ADS_CONSENT_STATE = stringPreferencesKey("ads_consent_state")
    val THEME_MODE = stringPreferencesKey("theme_mode")
}
