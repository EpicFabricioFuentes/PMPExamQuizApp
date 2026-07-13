package com.fax.passyourpmpexam.feature.free

import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.Question

/**
 * State for Free Practice. An empty [selectedDomains] means "all domains".
 * [question] is null when no installed question matches the current filter.
 */
data class FreeUiState(
    val selectedDomains: Set<Domain> = emptySet(),
    val question: Question? = null,
    val selectedIndex: Int? = null,
    val answered: Boolean = false,
    val isCorrect: Boolean? = null,
    val loading: Boolean = true,
    /** Non-null when the question pool failed to load; the screen offers a retry. */
    val error: String? = null,
) {
    val availableDomains: List<Domain> = Domain.entries
}
