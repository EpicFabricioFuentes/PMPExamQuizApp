package com.fax.passyourpmpexam.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A quiz attempt session. Persisted throughout play so it survives process death (resume). */
@Entity(tableName = "quiz_sessions")
data class QuizSessionEntity(
    @PrimaryKey val id: String,
    val certificationId: String,
    val type: String,
    val status: String,
    val configJson: String,
    val timeLimitMillis: Long?,
    val elapsedMillis: Long,
    val currentIndex: Int,
    val scorePercent: Int?,
    val passed: Boolean?,
    val createdAt: Long,
    val completedAt: Long?,
    val updatedAt: Long,
    val syncState: String,
)
