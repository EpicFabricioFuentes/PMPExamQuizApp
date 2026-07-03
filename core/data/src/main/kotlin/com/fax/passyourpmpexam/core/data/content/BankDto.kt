package com.fax.passyourpmpexam.core.data.content

import kotlinx.serialization.Serializable

/** Wire format for the bundled bank JSON (`assets/banks/pmp.json`). */
@Serializable
data class BankDto(
    val bankVersion: Int,
    val certificationId: String,
    val questions: List<QuestionDto>,
)

@Serializable
data class QuestionDto(
    val id: String,
    val domain: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
)
