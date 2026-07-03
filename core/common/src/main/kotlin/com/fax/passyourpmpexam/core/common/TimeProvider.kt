package com.fax.passyourpmpexam.core.common

import java.time.LocalDate

/** Abstracts "now" so time-dependent logic (streaks, daily question) is testable. */
interface TimeProvider {
    /** Days since 1970-01-01 in the device's default time zone. */
    fun todayEpochDay(): Long

    /** Epoch milliseconds. */
    fun nowMillis(): Long
}

class SystemTimeProvider : TimeProvider {
    override fun todayEpochDay(): Long = LocalDate.now().toEpochDay()
    override fun nowMillis(): Long = System.currentTimeMillis()
}
