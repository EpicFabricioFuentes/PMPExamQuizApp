package com.fax.passyourpmpexam.core.domain.model

/**
 * A single exam question. Every PMP question has exactly four options and one correct answer.
 */
data class Question(
    val id: String,
    val certificationId: String,
    val domain: Domain,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val bankVersion: Int,
) {
    init {
        require(options.size == OPTION_COUNT) { "A question must have exactly $OPTION_COUNT options" }
        require(correctIndex in options.indices) { "correctIndex $correctIndex is out of range" }
    }

    val correctOption: String get() = options[correctIndex]

    fun isCorrect(selectedIndex: Int): Boolean = selectedIndex == correctIndex

    companion object {
        const val OPTION_COUNT = 4
    }
}
