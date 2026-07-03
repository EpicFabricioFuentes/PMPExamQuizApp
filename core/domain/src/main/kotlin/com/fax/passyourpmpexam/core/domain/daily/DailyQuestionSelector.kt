package com.fax.passyourpmpexam.core.domain.daily

import com.fax.passyourpmpexam.core.domain.model.Question

/**
 * Picks the Daily Question deterministically from the [epochDay], so it is stable for a given day
 * (no re-roll on app restart) without needing to store extra state. Questions in [recentIds] are
 * avoided where the pool allows, to reduce short-term repeats.
 */
object DailyQuestionSelector {

    fun pick(pool: List<Question>, epochDay: Long, recentIds: Set<String> = emptySet()): Question? {
        if (pool.isEmpty()) return null
        val eligible = pool.filterNot { it.id in recentIds }.ifEmpty { pool }
        val ordered = eligible.sortedBy { it.id } // stable, deterministic ordering
        val index = epochDay.mod(ordered.size.toLong()).toInt()
        return ordered[index]
    }
}
