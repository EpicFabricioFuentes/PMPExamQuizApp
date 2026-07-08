package com.fax.passyourpmpexam.core.domain.model

/**
 * The three PMP exam content domains and their blueprint weights, used for the
 * weighted 180-question mock draw (People 33% / Process 41% / Business Environment 26% According to the July 2026 revision of the exam).
 *
 * [STANDARD_FOR_PM] is a foundational study category (content from The Standard for Project
 * Management), not a scored exam domain: its blueprint weight is 0.0, so it is excluded from the
 * weighted mock draw while remaining available as a filter in Free Practice.
 */
enum class Domain(val displayName: String, val blueprintWeight: Double) {
    PEOPLE("People", 0.33),
    PROCESS("Process", 0.41),
    BUSINESS_ENVIRONMENT("Business Environment", 0.26),
    STANDARD_FOR_PM("Standard for PM", 0.0),
}
