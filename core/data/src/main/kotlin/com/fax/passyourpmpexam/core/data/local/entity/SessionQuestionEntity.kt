package com.fax.passyourpmpexam.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Planned order + progressive answer for one question in a session. Powers resume and the results review list. */
@Entity(
    tableName = "session_questions",
    primaryKeys = ["sessionId", "orderIndex"],
    foreignKeys = [
        ForeignKey(
            entity = QuizSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
data class SessionQuestionEntity(
    val sessionId: String,
    val orderIndex: Int,
    val questionId: String,
    val selectedIndex: Int?,
    val isCorrect: Boolean?,
)
