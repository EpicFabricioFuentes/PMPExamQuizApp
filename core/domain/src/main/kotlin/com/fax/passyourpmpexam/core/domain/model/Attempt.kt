package com.fax.passyourpmpexam.core.domain.model

/**
 * A single graded answer, across any mode. This is the source of truth for aggregated stats.
 * [sessionId] is null for standalone answers (Daily / Free); set for answers within a quiz.
 * [domain] is denormalized here so stats queries don't need to join back to the question.
 */
data class Attempt(
    val id: String,
    val questionId: String,
    val sessionId: String?,
    val domain: Domain,
    val mode: QuizMode,
    val selectedIndex: Int,
    val isCorrect: Boolean,
    val answeredAt: Long,
)
