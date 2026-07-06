package com.fax.passyourpmpexam.core.data.repository

import com.fax.passyourpmpexam.core.data.local.dao.AttemptDao
import com.fax.passyourpmpexam.core.data.mapper.toDomain
import com.fax.passyourpmpexam.core.data.mapper.toEntity
import com.fax.passyourpmpexam.core.domain.model.Attempt
import com.fax.passyourpmpexam.core.domain.repository.AttemptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AttemptRepositoryImpl(
    private val dao: AttemptDao,
) : AttemptRepository {

    override suspend fun record(attempt: Attempt) = dao.insert(attempt.toEntity())

    override suspend fun recordAll(attempts: List<Attempt>) =
        dao.insertAll(attempts.map { it.toEntity() })

    override fun observeAll(): Flow<List<Attempt>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun totalCount(): Int = dao.totalCount()

    override suspend fun correctCount(): Int = dao.correctCount()

    override fun observeAnsweredCountBetween(startMillis: Long, endMillis: Long): Flow<Int> =
        dao.observeCountBetween(startMillis, endMillis)
}
