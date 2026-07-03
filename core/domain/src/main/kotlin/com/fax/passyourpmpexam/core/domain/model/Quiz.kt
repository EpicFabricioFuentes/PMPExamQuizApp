package com.fax.passyourpmpexam.core.domain.model

/**
 * The kinds of quiz session. Short quizzes and the full mock are timed; the mock mirrors the
 * real PMP exam (180 questions / 230 minutes). Short-quiz limits follow the same ~1.28 min/question
 * pace. Free mode is untimed and unbounded.
 */
enum class QuizType(val questionCount: Int?, val timeLimitMinutes: Int?) {
    SHORT_10(10, 13),
    SHORT_25(25, 32),
    SHORT_50(50, 64),
    MOCK_180(180, 230),
    FREE(null, null);

    val timeLimitMillis: Long? get() = timeLimitMinutes?.let { it * 60_000L }
}

/** Where an answer was recorded — used to tag attempts for aggregated stats. */
enum class QuizMode { DAILY, QUIZ, FREE }

enum class QuizStatus { IN_PROGRESS, COMPLETED, ABANDONED }

/** Configuration for a quiz/free session. An empty [domainFilter] means "all domains". */
data class QuizConfig(
    val type: QuizType,
    val domainFilter: Set<Domain> = emptySet(),
)

/** One question slot within a session, plus the user's chosen option (if answered). */
data class SessionQuestion(
    val question: Question,
    val selectedIndex: Int? = null,
) {
    val isAnswered: Boolean get() = selectedIndex != null
    val isCorrect: Boolean? get() = selectedIndex?.let { question.isCorrect(it) }
}

/**
 * A quiz session, including its planned (ordered) questions, progress cursor, and timer state.
 * [elapsedMillis] accrues only while the quiz is foregrounded, so it survives process death as a pause.
 */
data class QuizSession(
    val id: String,
    val config: QuizConfig,
    val questions: List<SessionQuestion>,
    val status: QuizStatus = QuizStatus.IN_PROGRESS,
    val currentIndex: Int = 0,
    val elapsedMillis: Long = 0L,
    val createdAt: Long = 0L,
    val completedAt: Long? = null,
)
