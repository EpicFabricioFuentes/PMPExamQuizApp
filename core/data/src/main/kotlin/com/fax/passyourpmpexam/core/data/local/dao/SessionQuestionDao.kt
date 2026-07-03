package com.fax.passyourpmpexam.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.fax.passyourpmpexam.core.data.local.entity.SessionQuestionEntity

@Dao
interface SessionQuestionDao {

    @Upsert
    suspend fun upsertAll(items: List<SessionQuestionEntity>)

    @Query("SELECT * FROM session_questions WHERE sessionId = :sessionId ORDER BY orderIndex")
    suspend fun getForSession(sessionId: String): List<SessionQuestionEntity>
}
