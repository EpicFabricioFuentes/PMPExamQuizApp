package com.fax.passyourpmpexam.core.data.content

import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.Question
import com.fax.passyourpmpexam.core.domain.model.QuestionBank
import com.fax.passyourpmpexam.core.domain.model.ThemeMode
import com.fax.passyourpmpexam.core.domain.repository.ContentSource
import com.fax.passyourpmpexam.core.domain.repository.QuestionRepository
import com.fax.passyourpmpexam.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BankImporterTest {

    @Test
    fun importsOnFirstRunAndRecordsVersion() = runTest {
        val content = FakeContentSource(bank(version = 6, count = 3))
        val questions = FakeQuestionRepository()
        val settings = FakeSettingsRepository(installedBankVersion = 0)
        val importer = BankImporter(content, questions, settings)

        val imported = importer.importIfNeeded()

        assertEquals(3, imported)
        assertEquals(3, questions.upserted.size)
        assertEquals(6, settings.installedBankVersion)
    }

    @Test
    fun isNoOpWhenAlreadyCurrent() = runTest {
        val content = FakeContentSource(bank(version = 6, count = 3))
        val questions = FakeQuestionRepository()
        val settings = FakeSettingsRepository(installedBankVersion = 6)
        val importer = BankImporter(content, questions, settings)

        val imported = importer.importIfNeeded()

        assertEquals(0, imported)
        assertEquals(0, questions.upserted.size)
        assertEquals(6, settings.installedBankVersion)
    }

    @Test
    fun reimportsWhenAssetVersionIsNewer() = runTest {
        val content = FakeContentSource(bank(version = 7, count = 2))
        val questions = FakeQuestionRepository()
        val settings = FakeSettingsRepository(installedBankVersion = 6)
        val importer = BankImporter(content, questions, settings)

        val imported = importer.importIfNeeded()

        assertEquals(2, imported)
        assertEquals(7, settings.installedBankVersion)
    }

    private fun bank(version: Int, count: Int) = QuestionBank(
        bankVersion = version,
        certificationId = "PMP",
        questions = List(count) { i ->
            Question(
                id = "q$i",
                certificationId = "PMP",
                domain = Domain.PROCESS,
                text = "t$i",
                options = listOf("a", "b", "c", "d"),
                correctIndex = 0,
                explanation = "e",
                bankVersion = version,
            )
        },
    )
}

private class FakeContentSource(private val bank: QuestionBank) : ContentSource {
    override suspend fun loadBank(): QuestionBank = bank
}

private class FakeQuestionRepository : QuestionRepository {
    val upserted = mutableListOf<Question>()
    override suspend fun getAll(certificationId: String): List<Question> = upserted
    override suspend fun getById(id: String): Question? = upserted.firstOrNull { it.id == id }
    override suspend fun count(): Int = upserted.size
    override suspend fun upsertAll(questions: List<Question>) {
        upserted += questions
    }
}

private class FakeSettingsRepository(var installedBankVersion: Int) : SettingsRepository {
    override suspend fun getInstalledBankVersion(): Int = installedBankVersion
    override suspend fun setInstalledBankVersion(version: Int) {
        installedBankVersion = version
    }
    override fun observeThemeMode(): Flow<ThemeMode> = flowOf(ThemeMode.SYSTEM)
    override suspend fun setThemeMode(mode: ThemeMode) = Unit
    override fun observeReminderEnabled(): Flow<Boolean> = flowOf(false)
    override suspend fun setReminderEnabled(enabled: Boolean) = Unit
    override fun observeReminderMinuteOfDay(): Flow<Int> = flowOf(0)
    override suspend fun setReminderMinuteOfDay(minuteOfDay: Int) = Unit
    override fun observeDailyGoal(): Flow<Int> = flowOf(1)
    override suspend fun setDailyGoal(goal: Int) = Unit
    override fun observeHasCompletedFirstRun(): Flow<Boolean> = flowOf(true)
    override suspend fun setFirstRunCompleted() = Unit
    override suspend fun getDailyLastAnsweredEpochDay(): Long = 0L
    override suspend fun setDailyLastAnsweredEpochDay(epochDay: Long) = Unit
}
