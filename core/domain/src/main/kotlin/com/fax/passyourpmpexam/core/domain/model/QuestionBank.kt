package com.fax.passyourpmpexam.core.domain.model

/** A parsed, validated question bank from a [com.fax.passyourpmpexam.core.domain.repository.ContentSource]. */
data class QuestionBank(
    val bankVersion: Int,
    val certificationId: String,
    val questions: List<Question>,
)
