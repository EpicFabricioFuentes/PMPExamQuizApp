package com.fax.passyourpmpexam.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fax.passyourpmpexam.core.data.local.entity.AttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttemptDao {

    @Insert
    suspend fun insert(attempt: AttemptEntity)

    @Insert
    suspend fun insertAll(attempts: List<AttemptEntity>)

    @Query("SELECT * FROM attempts ORDER BY answeredAt DESC")
    fun observeAll(): Flow<List<AttemptEntity>>

    @Query("SELECT COUNT(*) FROM attempts WHERE isCorrect = 1")
    suspend fun correctCount(): Int

    @Query("SELECT COUNT(*) FROM attempts")
    suspend fun totalCount(): Int
}
