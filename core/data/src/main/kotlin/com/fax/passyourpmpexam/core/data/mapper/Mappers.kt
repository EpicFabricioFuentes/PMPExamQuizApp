package com.fax.passyourpmpexam.core.data.mapper

import com.fax.passyourpmpexam.core.data.local.entity.AttemptEntity
import com.fax.passyourpmpexam.core.data.local.entity.QuestionEntity
import com.fax.passyourpmpexam.core.domain.model.Attempt
import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.Question
import com.fax.passyourpmpexam.core.domain.model.QuizMode

/** Default sync state for locally-created rows (cloud-sync seam). */
internal const val SYNC_LOCAL_ONLY = "LOCAL_ONLY"

fun QuestionEntity.toDomain(): Question =
    Question(
        id = id,
        certificationId = certificationId,
        domain = Domain.valueOf(domain),
        text = text,
        options = options,
        correctIndex = correctIndex,
        explanation = explanation,
        bankVersion = bankVersion,
    )

fun Question.toEntity(): QuestionEntity =
    QuestionEntity(
        id = id,
        certificationId = certificationId,
        domain = domain.name,
        text = text,
        options = options,
        correctIndex = correctIndex,
        explanation = explanation,
        bankVersion = bankVersion,
    )

fun AttemptEntity.toDomain(): Attempt =
    Attempt(
        id = id,
        questionId = questionId,
        sessionId = sessionId,
        domain = Domain.valueOf(domain),
        mode = QuizMode.valueOf(mode),
        selectedIndex = selectedIndex,
        isCorrect = isCorrect,
        answeredAt = answeredAt,
    )

fun Attempt.toEntity(): AttemptEntity =
    AttemptEntity(
        id = id,
        questionId = questionId,
        sessionId = sessionId,
        domain = domain.name,
        mode = mode.name,
        selectedIndex = selectedIndex,
        isCorrect = isCorrect,
        answeredAt = answeredAt,
        updatedAt = answeredAt,
        syncState = SYNC_LOCAL_ONLY,
    )
