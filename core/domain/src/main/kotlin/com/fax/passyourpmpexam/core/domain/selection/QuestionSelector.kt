package com.fax.passyourpmpexam.core.domain.selection

import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.Question
import com.fax.passyourpmpexam.core.domain.model.QuizConfig
import com.fax.passyourpmpexam.core.domain.model.QuizType
import kotlin.math.floor
import kotlin.random.Random

/**
 * Pure question-selection logic. Given a candidate [pool] and a [QuizConfig], produces the ordered
 * list of questions for a session. Deterministic for a given [Random] seed (for tests). Never repeats
 * a question within a session.
 */
object QuestionSelector {

    fun select(pool: List<Question>, config: QuizConfig, random: Random): List<Question> {
        val filtered = if (config.domainFilter.isEmpty()) {
            pool
        } else {
            pool.filter { it.domain in config.domainFilter }
        }
        return when (config.type) {
            QuizType.MOCK_180 -> blueprintWeighted(filtered, total = 180, random = random)
            QuizType.FREE -> filtered.shuffled(random)
            else -> {
                val n = config.type.questionCount ?: filtered.size
                filtered.shuffled(random).take(n)
            }
        }
    }

    /**
     * Distributes [total] questions across the domains by their blueprint weight, correcting rounding
     * so the parts sum exactly to [total] (e.g. 180 -> People 76 / Process 90 / Business Env 14).
     */
    fun blueprintCounts(total: Int): Map<Domain, Int> {
        val exact = Domain.entries.associateWith { it.blueprintWeight * total }
        val counts = exact.mapValues { it.value.toInt() }.toMutableMap()
        var remainder = total - counts.values.sum()
        // Hand the leftover units to the domains with the largest fractional parts.
        val byFraction = exact.entries.sortedByDescending { it.value - floor(it.value) }
        var i = 0
        while (remainder > 0) {
            val domain = byFraction[i % byFraction.size].key
            counts[domain] = counts.getValue(domain) + 1
            remainder--
            i++
        }
        return counts
    }

    private fun blueprintWeighted(pool: List<Question>, total: Int, random: Random): List<Question> {
        val counts = blueprintCounts(total)
        val byDomain = pool.groupBy { it.domain }
        val picked = mutableListOf<Question>()
        val leftovers = mutableListOf<Question>()
        for (domain in Domain.entries) {
            val available = (byDomain[domain] ?: emptyList()).shuffled(random)
            val need = counts[domain] ?: 0
            picked += available.take(need)
            leftovers += available.drop(need)
        }
        // If a domain lacked enough questions, backfill from the remaining pool.
        val shortfall = total - picked.size
        if (shortfall > 0) picked += leftovers.shuffled(random).take(shortfall)
        return picked.shuffled(random)
    }
}
