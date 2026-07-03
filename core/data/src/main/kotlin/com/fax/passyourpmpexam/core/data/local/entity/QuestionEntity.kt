package com.fax.passyourpmpexam.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Bundled question bank row. [options] is stored as a JSON string via [com.fax.passyourpmpexam.core.data.local.Converters]. */
@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val certificationId: String,
    val domain: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val bankVersion: Int,
)
