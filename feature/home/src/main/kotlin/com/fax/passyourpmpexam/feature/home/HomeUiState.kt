package com.fax.passyourpmpexam.feature.home

/** State for the Home hub. */
data class HomeUiState(
    val streakCount: Int = 0,
    val dailyCompletedToday: Boolean = false,
)
