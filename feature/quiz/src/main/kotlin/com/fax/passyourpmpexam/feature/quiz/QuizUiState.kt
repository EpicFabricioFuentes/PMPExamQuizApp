package com.fax.passyourpmpexam.feature.quiz

import com.fax.passyourpmpexam.core.domain.model.QuizSession
import com.fax.passyourpmpexam.core.domain.model.QuizType
import com.fax.passyourpmpexam.core.domain.model.ScoreResult

/** MVI state for the Quiz feature across its three phases. */
sealed interface QuizUiState {

    data object Loading : QuizUiState

    /** No questions installed. */
    data object Empty : QuizUiState

    /** Loading the pool / saved session failed; distinct from [Empty] so the user can retry. */
    data class Error(val message: String) : QuizUiState

    /** A saved in-progress session was found on launch; offer to resume or discard it. */
    data class ResumePrompt(val session: QuizSession) : QuizUiState

    /** Choosing quiz length before starting. */
    data class Setup(val selectedType: QuizType) : QuizUiState

    /** Answering. [remainingMillis] is null for untimed configs. */
    data class InProgress(
        val session: QuizSession,
        val remainingMillis: Long?,
    ) : QuizUiState {
        val currentIndex: Int get() = session.currentIndex
        val total: Int get() = session.questions.size
    }

    /** Graded outcome + the completed session for the review list. */
    data class Results(
        val result: ScoreResult,
        val session: QuizSession,
    ) : QuizUiState
}
