package com.fax.passyourpmpexam.core.domain.repository

import com.fax.passyourpmpexam.core.domain.model.Attempt
import com.fax.passyourpmpexam.core.domain.model.Question
import com.fax.passyourpmpexam.core.domain.model.QuestionBank
import com.fax.passyourpmpexam.core.domain.model.QuizSession
import com.fax.passyourpmpexam.core.domain.model.QuizStatus
import com.fax.passyourpmpexam.core.domain.model.StreakState
import com.fax.passyourpmpexam.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

const val DEFAULT_CERTIFICATION_ID: String = "PMP"

/** Provides the question bank content (bundled asset now; remote seam later). */
interface ContentSource {
    suspend fun loadBank(): QuestionBank
}

/** The installed question pool. */
interface QuestionRepository {
    suspend fun getAll(certificationId: String = DEFAULT_CERTIFICATION_ID): List<Question>
    suspend fun getById(id: String): Question?
    suspend fun count(): Int
    suspend fun upsertAll(questions: List<Question>)
}

/** Graded answers across all modes — the source of truth for aggregated stats. */
interface AttemptRepository {
    suspend fun record(attempt: Attempt)
    suspend fun recordAll(attempts: List<Attempt>)
    fun observeAll(): Flow<List<Attempt>>
    suspend fun totalCount(): Int
    suspend fun correctCount(): Int
}

/** Persisted quiz sessions — powers process-death resume and the results review list. */
interface QuizSessionRepository {
    suspend fun save(session: QuizSession)
    suspend fun getById(id: String): QuizSession?

    /** The most recent session in the given [status] (default: a resumable in-progress one), or null. */
    suspend fun getLatestByStatus(status: QuizStatus = QuizStatus.IN_PROGRESS): QuizSession?
}

/** Streak scalars, persisted in DataStore. */
interface StreakRepository {
    fun observe(): Flow<StreakState>
    suspend fun get(): StreakState
    suspend fun set(state: StreakState)
}

/** User settings + bank-version bookkeeping, persisted in DataStore. */
interface SettingsRepository {
    fun observeThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)

    fun observeReminderEnabled(): Flow<Boolean>
    suspend fun setReminderEnabled(enabled: Boolean)

    fun observeReminderMinuteOfDay(): Flow<Int>
    suspend fun setReminderMinuteOfDay(minuteOfDay: Int)

    suspend fun getInstalledBankVersion(): Int
    suspend fun setInstalledBankVersion(version: Int)

    /** Whether the one-time first-run welcome has been dismissed; gates the welcome screen. */
    fun observeHasCompletedFirstRun(): Flow<Boolean>
    suspend fun setFirstRunCompleted()

    /** Daily-question bookkeeping so it stays stable for the day and streak isn't double-counted. */
    suspend fun getDailyLastAnsweredEpochDay(): Long
    suspend fun setDailyLastAnsweredEpochDay(epochDay: Long)
}
