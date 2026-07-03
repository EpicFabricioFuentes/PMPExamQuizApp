package com.fax.passyourpmpexam.core.data.content

import com.fax.passyourpmpexam.core.domain.repository.ContentSource
import com.fax.passyourpmpexam.core.domain.repository.QuestionRepository
import com.fax.passyourpmpexam.core.domain.repository.SettingsRepository

/**
 * Imports the bundled bank into Room on first run, or when the asset's [bankVersion] exceeds the
 * installed one. Idempotent: a no-op (returns 0) when already up to date.
 */
class BankImporter(
    private val contentSource: ContentSource,
    private val questionRepository: QuestionRepository,
    private val settingsRepository: SettingsRepository,
) {
    /** @return the number of questions imported (0 when already current). */
    suspend fun importIfNeeded(): Int {
        val bank = contentSource.loadBank()
        val installed = settingsRepository.getInstalledBankVersion()
        if (bank.bankVersion <= installed) return 0

        questionRepository.upsertAll(bank.questions)
        settingsRepository.setInstalledBankVersion(bank.bankVersion)
        return bank.questions.size
    }
}
