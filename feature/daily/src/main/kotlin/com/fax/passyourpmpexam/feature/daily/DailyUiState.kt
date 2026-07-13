package com.fax.passyourpmpexam.feature.daily

import com.fax.passyourpmpexam.core.domain.model.Question

/** MVI state for the Daily Question screen. */
sealed interface DailyUiState {

    data object Loading : DailyUiState

    /** No questions are installed (bank not yet imported / empty). */
    data object Empty : DailyUiState

    /** A read failed (e.g. the local store threw); distinct from [Empty] so the user can retry. */
    data class Error(val message: String) : DailyUiState

    data class Ready(
        val question: Question,
        val selectedIndex: Int?,
        val answered: Boolean,
        val isCorrect: Boolean?,
        /** True when the user had already completed today's question before this screen opened. */
        val alreadyCompletedToday: Boolean,
    ) : DailyUiState
}
