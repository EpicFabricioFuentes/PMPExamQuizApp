package com.fax.passyourpmpexam.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.fax.passyourpmpexam.core.data.local.entity.QuizSessionEntity

@Dao
interface QuizSessionDao {

    @Upsert
    suspend fun upsert(session: QuizSessionEntity)

    @Query("SELECT * FROM quiz_sessions WHERE id = :id")
    suspend fun getById(id: String): QuizSessionEntity?

    @Query("SELECT * FROM quiz_sessions WHERE status = :status ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestByStatus(status: String): QuizSessionEntity?
}
