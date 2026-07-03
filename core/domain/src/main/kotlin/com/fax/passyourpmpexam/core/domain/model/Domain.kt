package com.fax.passyourpmpexam.core.domain.model

/**
 * The three PMP exam content domains and their blueprint weights, used for the
 * weighted 180-question mock draw (People 42% / Process 50% / Business Environment 8%).
 */
enum class Domain(val displayName: String, val blueprintWeight: Double) {
    PEOPLE("People", 0.42),
    PROCESS("Process", 0.50),
    BUSINESS_ENVIRONMENT("Business Environment", 0.08),
}
