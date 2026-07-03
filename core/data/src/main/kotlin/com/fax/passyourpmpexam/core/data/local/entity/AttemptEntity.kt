package com.fax.passyourpmpexam.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Single source of truth for aggregated stats across all modes (domain denormalized for fast queries). */
@Entity(
    tableName = "attempts",
    indices = [Index("domain"), Index("questionId")],
)
data class AttemptEntity(
    @PrimaryKey val id: String,
    val questionId: String,
    val sessionId: String?,
    val domain: String,
    val mode: String,
    val selectedIndex: Int,
    val isCorrect: Boolean,
    val answeredAt: Long,
    val updatedAt: Long,
    val syncState: String,
)
