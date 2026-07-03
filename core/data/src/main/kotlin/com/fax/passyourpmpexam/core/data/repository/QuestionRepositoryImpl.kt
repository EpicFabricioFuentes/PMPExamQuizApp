package com.fax.passyourpmpexam.core.data.repository

import com.fax.passyourpmpexam.core.data.local.dao.QuestionDao
import com.fax.passyourpmpexam.core.data.mapper.toDomain
import com.fax.passyourpmpexam.core.data.mapper.toEntity
import com.fax.passyourpmpexam.core.domain.model.Question
import com.fax.passyourpmpexam.core.domain.repository.QuestionRepository

class QuestionRepositoryImpl(
    private val dao: QuestionDao,
) : QuestionRepository {

    override suspend fun getAll(certificationId: String): List<Question> =
        dao.getAll(certificationId).map { it.toDomain() }

    override suspend fun getById(id: String): Question? =
        dao.getById(id)?.toDomain()

    override suspend fun count(): Int = dao.count()

    override suspend fun upsertAll(questions: List<Question>) =
        dao.upsertAll(questions.map { it.toEntity() })
}
