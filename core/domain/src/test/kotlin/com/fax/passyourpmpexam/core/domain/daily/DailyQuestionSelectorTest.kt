package com.fax.passyourpmpexam.core.domain.daily

import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.Question
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class DailyQuestionSelectorTest {

    private fun q(id: String) =
        Question(id, "PMP", Domain.PROCESS, "text", listOf("a", "b", "c", "d"), 0, "explanation", 1)

    private val pool = (0..9).map { q("q$it") }

    @Test
    fun sameDayYieldsSameQuestion() {
        assertEquals(
            DailyQuestionSelector.pick(pool, epochDay = 100)?.id,
            DailyQuestionSelector.pick(pool, epochDay = 100)?.id,
        )
    }

    @Test
    fun differentDaysGenerallyDiffer() {
        val d1 = DailyQuestionSelector.pick(pool, epochDay = 100)?.id
        val d2 = DailyQuestionSelector.pick(pool, epochDay = 101)?.id
        assertNotEquals(d1, d2)
    }

    @Test
    fun avoidsRecentQuestions() {
        val today = DailyQuestionSelector.pick(pool, epochDay = 100)!!
        val next = DailyQuestionSelector.pick(pool, epochDay = 100, recentIds = setOf(today.id))
        assertNotEquals(today.id, next?.id)
    }

    @Test
    fun emptyPoolReturnsNull() {
        assertNull(DailyQuestionSelector.pick(emptyList(), epochDay = 100))
    }
}
