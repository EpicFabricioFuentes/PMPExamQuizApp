package com.fax.passyourpmpexam.core.common

/**
 * Minimal result type for operations that can fail with a domain-level error.
 * Kept stdlib-only so it stays KMP-portable.
 */
sealed interface Outcome<out T> {
    data class Success<out T>(val value: T) : Outcome<T>
    data class Failure(val error: AppError) : Outcome<Nothing>
}

/** Coarse, presentation-agnostic error categories. */
sealed interface AppError {
    data object NotFound : AppError
    data class Unexpected(val message: String?) : AppError
}
