package com.fax.passyourpmpexam.core.domain.model

/** User theme preference. Overrides the system default; persisted in DataStore. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    ;

    companion object {
        val DEFAULT: ThemeMode = SYSTEM

        /** Parses a stored name, falling back to [DEFAULT] for unknown/empty values. */
        fun fromStorage(value: String?): ThemeMode =
            entries.firstOrNull { it.name == value } ?: DEFAULT
    }
}
