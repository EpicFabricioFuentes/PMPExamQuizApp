package com.fax.passyourpmpexam.feature.quiz

import com.fax.passyourpmpexam.core.domain.model.QuizType

sealed interface QuizIntent {
    data class SelectType(val type: QuizType) : QuizIntent
    data object Start : QuizIntent
    data object ResumeSaved : QuizIntent
    data object DiscardSaved : QuizIntent
    data class SelectOption(val index: Int) : QuizIntent
    data object Next : QuizIntent
    data object Previous : QuizIntent
    data object Submit : QuizIntent
    data object Restart : QuizIntent

    /** Abandon the in-progress quiz and return to the setup screen (back arrow while answering). */
    data object ExitToSetup : QuizIntent

    /** Reload the pool / saved session after an error state. */
    data object Retry : QuizIntent
}
