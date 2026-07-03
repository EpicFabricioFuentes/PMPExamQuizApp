package com.fax.passyourpmpexam.core.domain.streak

import com.fax.passyourpmpexam.core.domain.model.StreakState
import kotlin.test.Test
import kotlin.test.assertEquals

class StreakCalculatorTest {

    @Test
    fun firstActivityStartsStreakAtOne() {
        val s = StreakCalculator.onActivity(StreakState(), todayEpochDay = 100)
        assertEquals(1, s.currentStreak)
        assertEquals(1, s.longestStreak)
        assertEquals(100, s.lastActivityEpochDay)
    }

    @Test
    fun sameDayActivityDoesNotIncrement() {
        var s = StreakCalculator.onActivity(StreakState(), 100)
        s = StreakCalculator.onActivity(s, 100)
        assertEquals(1, s.currentStreak)
    }

    @Test
    fun consecutiveDayIncrements() {
        var s = StreakCalculator.onActivity(StreakState(), 100)
        s = StreakCalculator.onActivity(s, 101)
        assertEquals(2, s.currentStreak)
        assertEquals(2, s.longestStreak)
    }

    @Test
    fun missedDayResetsToOneButKeepsLongest() {
        var s = StreakCalculator.onActivity(StreakState(), 100)
        s = StreakCalculator.onActivity(s, 101) // streak = 2
        s = StreakCalculator.onActivity(s, 104) // gap of 3 days -> reset
        assertEquals(1, s.currentStreak)
        assertEquals(2, s.longestStreak)
    }

    @Test
    fun displayStreakReflectsLapse() {
        val s = StreakCalculator.onActivity(StreakState(), 100) // current = 1, last = 100
        assertEquals(1, StreakCalculator.displayStreak(s, 100)) // same day
        assertEquals(1, StreakCalculator.displayStreak(s, 101)) // yesterday -> still alive
        assertEquals(0, StreakCalculator.displayStreak(s, 102)) // gap 2 -> lapsed
    }

    @Test
    fun displayStreakIsZeroWithNoHistory() {
        assertEquals(0, StreakCalculator.displayStreak(StreakState(), 100))
    }
}
