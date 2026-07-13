package com.fax.passyourpmpexam.core.data.mapper

import com.fax.passyourpmpexam.core.domain.model.Attempt
import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.Question
import com.fax.passyourpmpexam.core.domain.model.QuizMode
import kotlin.test.Test
import kotlin.test.assertEquals

class MappersTest {

    @Test
    fun questionRoundTripsThroughEntity() {
        val question = Question(
            id = "q1",
            certificationId = "PMP",
            domain = Domain.BUSINESS_ENVIRONMENT,
            text = "Which is a business document?",
            options = listOf("A", "B", "C", "D"),
            correctIndex = 2,
            explanation = "Because...",
            bankVersion = 6,
        )

        assertEquals(question, question.toEntity().toDomain())
    }

    @Test
    fun questionEntityMapsDomainEnumByName() {
        val entity = Question(
            id = "q2",
            certificationId = "PMP",
            domain = Domain.PEOPLE,
            text = "t",
            options = listOf("a", "b", "c", "d"),
            correctIndex = 0,
            explanation = "e",
            bankVersion = 1,
        ).toEntity()

        assertEquals("PEOPLE", entity.domain)
        assertEquals(Domain.PEOPLE, entity.toDomain().domain)
    }

    @Test
    fun attemptToEntityStampsLocalOnlySyncAndUpdatedAt() {
        val attempt = Attempt(
            id = "a1",
            questionId = "q1",
            sessionId = null,
            domain = Domain.PROCESS,
            mode = QuizMode.DAILY,
            selectedIndex = 1,
            isCorrect = true,
            answeredAt = 1_700_000_000_000L,
        )

        val entity = attempt.toEntity()

        assertEquals(SYNC_LOCAL_ONLY, entity.syncState)
        // updatedAt is seeded from answeredAt for locally-created rows.
        assertEquals(attempt.answeredAt, entity.updatedAt)
        // Domain data survives the round-trip.
        assertEquals(attempt, entity.toDomain())
    }
}
