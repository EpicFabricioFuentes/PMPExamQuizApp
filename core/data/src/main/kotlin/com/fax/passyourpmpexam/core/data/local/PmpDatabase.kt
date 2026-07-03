package com.fax.passyourpmpexam.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fax.passyourpmpexam.core.data.local.dao.AttemptDao
import com.fax.passyourpmpexam.core.data.local.dao.QuestionDao
import com.fax.passyourpmpexam.core.data.local.dao.QuizSessionDao
import com.fax.passyourpmpexam.core.data.local.dao.SessionQuestionDao
import com.fax.passyourpmpexam.core.data.local.entity.AttemptEntity
import com.fax.passyourpmpexam.core.data.local.entity.QuestionEntity
import com.fax.passyourpmpexam.core.data.local.entity.QuizSessionEntity
import com.fax.passyourpmpexam.core.data.local.entity.SessionQuestionEntity

@Database(
    entities = [
        QuestionEntity::class,
        QuizSessionEntity::class,
        SessionQuestionEntity::class,
        AttemptEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class PmpDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun quizSessionDao(): QuizSessionDao
    abstract fun sessionQuestionDao(): SessionQuestionDao
    abstract fun attemptDao(): AttemptDao

    companion object {
        const val NAME = "pmp.db"
    }
}
