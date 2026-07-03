package com.fax.passyourpmpexam.core.domain.model

import kotlin.math.roundToInt

/** Per-domain correctness, used for the Results screen breakdown bars. */
data class DomainScore(val correct: Int, val total: Int) {
    val percent: Int get() = if (total == 0) 0 else (correct * 100.0 / total).roundToInt()
}

/** The computed outcome of a completed quiz session. */
data class ScoreResult(
    val correct: Int,
    val total: Int,
    val percent: Int,
    val passed: Boolean,
    val perDomain: Map<Domain, DomainScore>,
    val elapsedMillis: Long,
    val timeLimitMillis: Long?,
)
