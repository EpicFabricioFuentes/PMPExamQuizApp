package com.fax.passyourpmpexam.core.notifications

import kotlin.test.Test
import kotlin.test.assertEquals

class ReminderTimingTest {

    private val minute = ReminderTiming.MILLIS_PER_MINUTE
    private val day = ReminderTiming.MILLIS_PER_DAY

    @Test
    fun targetLaterTodayReturnsDelayUntilLaterToday() {
        // now 08:00 (480 min), target 20:00 (1200 min) -> 12h ahead
        val delay = ReminderTiming.delayMillisUntilNext(nowMillisOfDay = 480 * minute, targetMinuteOfDay = 1200)
        assertEquals(720 * minute, delay)
    }

    @Test
    fun targetEarlierTodayRollsToTomorrow() {
        // now 20:00, target 08:00 -> next occurrence is tomorrow (20h ahead)
        val delay = ReminderTiming.delayMillisUntilNext(nowMillisOfDay = 1200 * minute, targetMinuteOfDay = 480)
        assertEquals(1200 * minute, delay)
    }

    @Test
    fun targetExactlyNowRollsToTomorrow() {
        val delay = ReminderTiming.delayMillisUntilNext(nowMillisOfDay = 600 * minute, targetMinuteOfDay = 600)
        assertEquals(day, delay)
    }

    @Test
    fun delayIsNeverZeroOrNegative() {
        for (target in 0 until (24 * 60) step 37) {
            val delay = ReminderTiming.delayMillisUntilNext(nowMillisOfDay = 600 * minute, targetMinuteOfDay = target)
            assert(delay in 1..day) { "delay $delay out of range for target $target" }
        }
    }
}
