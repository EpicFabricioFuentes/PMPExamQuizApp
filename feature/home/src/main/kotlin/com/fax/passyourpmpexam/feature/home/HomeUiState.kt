package com.fax.passyourpmpexam.feature.home

import com.fax.passyourpmpexam.core.domain.model.DailyGoal

/** State for the Home hub. */
data class HomeUiState(
    val streakCount: Int = 0,
    /** Questions answered today across all modes. */
    val answeredToday: Int = 0,
    /** The user's cross-mode daily target. */
    val dailyGoal: Int = DailyGoal.DEFAULT,
    val questionCount: Int = 0,
)
