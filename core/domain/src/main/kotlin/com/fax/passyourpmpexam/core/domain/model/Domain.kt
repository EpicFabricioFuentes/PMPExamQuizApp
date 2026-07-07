package com.fax.passyourpmpexam.core.domain.model

/**
 * The three PMP exam content domains and their blueprint weights, used for the
 * weighted 180-question mock draw (People 33% / Process 41% / Business Environment 26% According to the July 2026 revision of the exam).
 */
enum class Domain(val displayName: String, val blueprintWeight: Double) {
    PEOPLE("People", 0.33),
    PROCESS("Process", 0.41),
    BUSINESS_ENVIRONMENT("Business Environment", 0.26),
}
