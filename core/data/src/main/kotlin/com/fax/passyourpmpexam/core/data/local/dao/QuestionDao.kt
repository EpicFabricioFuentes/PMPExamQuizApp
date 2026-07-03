package com.fax.passyourpmpexam.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.fax.passyourpmpexam.core.data.local.entity.QuestionEntity

@Dao
interface QuestionDao {

    @Upsert
    suspend fun upsertAll(questions: List<QuestionEntity>)

    @Query("SELECT * FROM questions WHERE certificationId = :certificationId")
    suspend fun getAll(certificationId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getById(id: String): QuestionEntity?

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun count(): Int
}
