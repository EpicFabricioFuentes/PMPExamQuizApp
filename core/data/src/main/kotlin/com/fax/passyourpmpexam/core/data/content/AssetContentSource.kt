package com.fax.passyourpmpexam.core.data.content

import android.content.Context
import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.Question
import com.fax.passyourpmpexam.core.domain.model.QuestionBank
import com.fax.passyourpmpexam.core.domain.repository.ContentSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Reads the bundled question bank from app assets and validates it. Validation: unique ids, a known
 * [Domain], exactly 4 options and an in-range correctIndex (both enforced by [Question]'s init).
 */
class AssetContentSource(
    private val context: Context,
    private val json: Json,
    private val assetPath: String = "banks/pmp.json",
) : ContentSource {

    override suspend fun loadBank(): QuestionBank = withContext(Dispatchers.IO) {
        val raw = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        val dto = json.decodeFromString<BankDto>(raw)

        val seenIds = HashSet<String>()
        val questions = dto.questions.map { q ->
            require(seenIds.add(q.id)) { "Duplicate question id: ${q.id}" }
            val domain = Domain.entries.firstOrNull { it.name == q.domain }
                ?: error("Unknown domain '${q.domain}' for question ${q.id}")
            Question(
                id = q.id,
                certificationId = dto.certificationId,
                domain = domain,
                text = q.text,
                options = q.options,
                correctIndex = q.correctIndex,
                explanation = q.explanation,
                bankVersion = dto.bankVersion,
            )
        }
        QuestionBank(dto.bankVersion, dto.certificationId, questions)
    }
}
