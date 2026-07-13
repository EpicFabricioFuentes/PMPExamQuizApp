package com.fax.passyourpmpexam.core.data.repository

import com.fax.passyourpmpexam.core.common.TimeProvider
import com.fax.passyourpmpexam.core.data.local.dao.QuestionDao
import com.fax.passyourpmpexam.core.data.local.dao.QuizSessionDao
import com.fax.passyourpmpexam.core.data.local.dao.SessionQuestionDao
import com.fax.passyourpmpexam.core.data.local.entity.QuizSessionEntity
import com.fax.passyourpmpexam.core.data.local.entity.SessionQuestionEntity
import com.fax.passyourpmpexam.core.data.mapper.SYNC_LOCAL_ONLY
import com.fax.passyourpmpexam.core.data.mapper.toDomain
import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.QuizConfig
import com.fax.passyourpmpexam.core.domain.model.QuizSession
import com.fax.passyourpmpexam.core.domain.model.QuizStatus
import com.fax.passyourpmpexam.core.domain.model.QuizType
import com.fax.passyourpmpexam.core.domain.model.SessionQuestion
import com.fax.passyourpmpexam.core.domain.repository.DEFAULT_CERTIFICATION_ID
import com.fax.passyourpmpexam.core.domain.repository.QuizSessionRepository
import com.fax.passyourpmpexam.core.domain.scoring.Scorer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class QuizSessionRepositoryImpl(
    private val sessionDao: QuizSessionDao,
    private val sessionQuestionDao: SessionQuestionDao,
    private val questionDao: QuestionDao,
    private val json: Json,
    private val timeProvider: TimeProvider,
) : QuizSessionRepository {

    @Serializable
    private data class ConfigJson(val domainFilter: List<String>)

    override suspend fun save(session: QuizSession) {
        val config = ConfigJson(session.config.domainFilter.map { it.name })
        // Persist the score on completed sessions so a finished quiz keeps its result across
        // process death / history reads (in-progress sessions have no meaningful score yet).
        val score = if (session.status == QuizStatus.COMPLETED) Scorer.score(session) else null
        sessionDao.upsert(
            QuizSessionEntity(
                id = session.id,
                certificationId = DEFAULT_CERTIFICATION_ID,
                type = session.config.type.name,
                status = session.status.name,
                configJson = json.encodeToString(config),
                timeLimitMillis = session.config.type.timeLimitMillis,
                elapsedMillis = session.elapsedMillis,
                currentIndex = session.currentIndex,
                scorePercent = score?.percent,
                passed = score?.passed,
                createdAt = session.createdAt,
                completedAt = session.completedAt,
                updatedAt = timeProvider.nowMillis(),
                syncState = SYNC_LOCAL_ONLY,
            ),
        )
        sessionQuestionDao.upsertAll(
            session.questions.mapIndexed { index, slot ->
                SessionQuestionEntity(
                    sessionId = session.id,
                    orderIndex = index,
                    questionId = slot.question.id,
                    selectedIndex = slot.selectedIndex,
                    isCorrect = slot.isCorrect,
                )
            },
        )
    }

    override suspend fun getById(id: String): QuizSession? {
        val entity = sessionDao.getById(id) ?: return null
        return entity.toDomain(sessionQuestionDao.getForSession(id))
    }

    override suspend fun getLatestByStatus(status: QuizStatus): QuizSession? {
        val entity = sessionDao.getLatestByStatus(status.name) ?: return null
        return entity.toDomain(sessionQuestionDao.getForSession(entity.id))
    }

    /** Rehydrates a session, resolving each planned slot's question from the bank (dropping any missing). */
    private suspend fun QuizSessionEntity.toDomain(rows: List<SessionQuestionEntity>): QuizSession {
        val slots = rows.mapNotNull { row ->
            val question = questionDao.getById(row.questionId)?.toDomain() ?: return@mapNotNull null
            SessionQuestion(question = question, selectedIndex = row.selectedIndex)
        }
        val filter = json.decodeFromString<ConfigJson>(configJson)
            .domainFilter.mapTo(mutableSetOf()) { Domain.valueOf(it) }
        return QuizSession(
            id = id,
            config = QuizConfig(type = QuizType.valueOf(type), domainFilter = filter),
            questions = slots,
            status = QuizStatus.valueOf(status),
            currentIndex = currentIndex,
            elapsedMillis = elapsedMillis,
            createdAt = createdAt,
            completedAt = completedAt,
        )
    }
}
