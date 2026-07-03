package com.fax.passyourpmpexam.core.domain.scoring

import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.DomainScore
import com.fax.passyourpmpexam.core.domain.model.Question
import com.fax.passyourpmpexam.core.domain.model.QuizConfig
import com.fax.passyourpmpexam.core.domain.model.QuizSession
import com.fax.passyourpmpexam.core.domain.model.QuizType
import com.fax.passyourpmpexam.core.domain.model.SessionQuestion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScorerTest {

    private var counter = 0

    private fun answered(domain: Domain, correct: Boolean): SessionQuestion {
        val question = Question(
            "q${counter++}", "PMP", domain, "text", listOf("a", "b", "c", "d"), 0, "explanation", 1,
        )
        return SessionQuestion(question, selectedIndex = if (correct) 0 else 1)
    }

    private fun session(slots: List<SessionQuestion>, elapsed: Long = 0L) =
        QuizSession(id = "s", config = QuizConfig(QuizType.MOCK_180), questions = slots, elapsedMillis = elapsed)

    @Test
    fun passesAtExactlyThreshold() {
        val slots = List(61) { answered(Domain.PROCESS, true) } + List(39) { answered(Domain.PROCESS, false) }
        val result = Scorer.score(session(slots))
        assertEquals(61, result.percent)
        assertTrue(result.passed)
    }

    @Test
    fun failsBelowThreshold() {
        val slots = List(60) { answered(Domain.PROCESS, true) } + List(40) { answered(Domain.PROCESS, false) }
        val result = Scorer.score(session(slots))
        assertEquals(60, result.percent)
        assertFalse(result.passed)
    }

    @Test
    fun computesPerDomainBreakdownAndOmitsUnusedDomains() {
        val slots = listOf(
            answered(Domain.PEOPLE, true),
            answered(Domain.PEOPLE, false),
            answered(Domain.PROCESS, true),
        )
        val result = Scorer.score(session(slots))
        assertEquals(DomainScore(1, 2), result.perDomain[Domain.PEOPLE])
        assertEquals(DomainScore(1, 1), result.perDomain[Domain.PROCESS])
        assertNull(result.perDomain[Domain.BUSINESS_ENVIRONMENT])
    }

    @Test
    fun carriesTimingThrough() {
        val result = Scorer.score(session(listOf(answered(Domain.PROCESS, true)), elapsed = 5_000L))
        assertEquals(5_000L, result.elapsedMillis)
        assertEquals(230 * 60_000L, result.timeLimitMillis)
    }
}
