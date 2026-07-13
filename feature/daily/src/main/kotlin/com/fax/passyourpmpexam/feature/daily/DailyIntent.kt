package com.fax.passyourpmpexam.feature.daily

/** User intents for the Daily Question screen. */
sealed interface DailyIntent {
    /** Choose an option. In Daily mode this immediately grades and reveals the explanation. */
    data class SelectOption(val index: Int) : DailyIntent

    /** Reload after an error state. */
    data object Retry : DailyIntent
}
