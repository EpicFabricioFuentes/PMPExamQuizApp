package com.fax.passyourpmpexam.feature.free

import com.fax.passyourpmpexam.core.domain.model.Domain

sealed interface FreeIntent {
    data class ToggleDomain(val domain: Domain) : FreeIntent
    data class SelectOption(val index: Int) : FreeIntent
    data object Next : FreeIntent
}
