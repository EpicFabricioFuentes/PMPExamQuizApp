package com.fax.passyourpmpexam.core.domain.selection

import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.Question
import com.fax.passyourpmpexam.core.domain.model.QuizConfig
import com.fax.passyourpmpexam.core.domain.model.QuizType
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuestionSelectorTest {

    private fun q(id: String, domain: Domain) =
        Question(id, "PMP", domain, "text", listOf("a", "b", "c", "d"), 0, "explanation", 1)

    @Test
    fun blueprintCountsSplit180IntoBlueprintWeights() {
        val counts = QuestionSelector.blueprintCounts(180)
        assertEquals(76, counts[Domain.PEOPLE])
        assertEquals(90, counts[Domain.PROCESS])
        assertEquals(14, counts[Domain.BUSINESS_ENVIRONMENT])
        assertEquals(180, counts.values.sum())
    }

    @Test
    fun mockDrawRespectsBlueprintAndHasNoDuplicates() {
        val pool = buildList {
            repeat(100) { add(q("p$it", Domain.PEOPLE)) }
            repeat(100) { add(q("pr$it", Domain.PROCESS)) }
            repeat(100) { add(q("b$it", Domain.BUSINESS_ENVIRONMENT)) }
        }
        val result = QuestionSelector.select(pool, QuizConfig(QuizType.MOCK_180), Random(42))
        assertEquals(180, result.size)
        assertEquals(180, result.map { it.id }.toSet().size)
        assertEquals(76, result.count { it.domain == Domain.PEOPLE })
        assertEquals(90, result.count { it.domain == Domain.PROCESS })
        assertEquals(14, result.count { it.domain == Domain.BUSINESS_ENVIRONMENT })
    }

    @Test
    fun shortQuizTakesRequestedCountAndAppliesDomainFilter() {
        val pool = buildList {
            repeat(30) { add(q("p$it", Domain.PEOPLE)) }
            repeat(30) { add(q("pr$it", Domain.PROCESS)) }
        }
        val result = QuestionSelector.select(
            pool,
            QuizConfig(QuizType.SHORT_25, domainFilter = setOf(Domain.PEOPLE)),
            Random(1),
        )
        assertEquals(25, result.size)
        assertTrue(result.all { it.domain == Domain.PEOPLE })
    }

    @Test
    fun selectionIsDeterministicForTheSameSeed() {
        val pool = (0 until 50).map { q("q$it", Domain.PROCESS) }
        val a = QuestionSelector.select(pool, QuizConfig(QuizType.SHORT_10), Random(7)).map { it.id }
        val b = QuestionSelector.select(pool, QuizConfig(QuizType.SHORT_10), Random(7)).map { it.id }
        assertEquals(a, b)
    }
}
