package com.fax.passyourpmpexam.core.domain.scoring

import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.DomainScore
import com.fax.passyourpmpexam.core.domain.model.QuizSession
import com.fax.passyourpmpexam.core.domain.model.ScoreResult
import kotlin.math.roundToInt

/**
 * Pure scoring for a completed quiz session: overall percent, pass/fail against the
 * fixed 61% threshold, and per-domain breakdown for the results bars.
 */
object Scorer {

    const val PASS_THRESHOLD_PERCENT = 61

    fun score(session: QuizSession): ScoreResult {
        val slots = session.questions
        val total = slots.size
        val correct = slots.count { it.isCorrect == true }
        val percent = if (total == 0) 0 else (correct * 100.0 / total).roundToInt()

        val perDomain = Domain.entries
            .associateWith { domain ->
                val inDomain = slots.filter { it.question.domain == domain }
                DomainScore(correct = inDomain.count { it.isCorrect == true }, total = inDomain.size)
            }
            .filterValues { it.total > 0 }

        return ScoreResult(
            correct = correct,
            total = total,
            percent = percent,
            passed = percent >= PASS_THRESHOLD_PERCENT,
            perDomain = perDomain,
            elapsedMillis = session.elapsedMillis,
            timeLimitMillis = session.config.type.timeLimitMillis,
        )
    }
}
