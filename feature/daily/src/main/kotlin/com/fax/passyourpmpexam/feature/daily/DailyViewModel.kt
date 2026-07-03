package com.fax.passyourpmpexam.feature.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fax.passyourpmpexam.core.common.IdGenerator
import com.fax.passyourpmpexam.core.common.TimeProvider
import com.fax.passyourpmpexam.core.domain.daily.DailyQuestionSelector
import com.fax.passyourpmpexam.core.domain.model.Attempt
import com.fax.passyourpmpexam.core.domain.model.QuizMode
import com.fax.passyourpmpexam.core.domain.repository.AttemptRepository
import com.fax.passyourpmpexam.core.domain.repository.QuestionRepository
import com.fax.passyourpmpexam.core.domain.repository.SettingsRepository
import com.fax.passyourpmpexam.core.domain.repository.StreakRepository
import com.fax.passyourpmpexam.core.domain.streak.StreakCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DailyViewModel(
    private val questionRepository: QuestionRepository,
    private val attemptRepository: AttemptRepository,
    private val streakRepository: StreakRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider,
    private val idGenerator: IdGenerator,
) : ViewModel() {

    private val _state = MutableStateFlow<DailyUiState>(DailyUiState.Loading)
    val state: StateFlow<DailyUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val pool = questionRepository.getAll()
            val today = timeProvider.todayEpochDay()
            val question = DailyQuestionSelector.pick(pool, today)
            if (question == null) {
                _state.value = DailyUiState.Empty
                return@launch
            }
            val alreadyCompleted = settingsRepository.getDailyLastAnsweredEpochDay() == today
            _state.value = DailyUiState.Ready(
                question = question,
                selectedIndex = null,
                answered = alreadyCompleted,
                isCorrect = null,
                alreadyCompletedToday = alreadyCompleted,
            )
        }
    }

    fun onIntent(intent: DailyIntent) {
        when (intent) {
            is DailyIntent.SelectOption -> submit(intent.index)
        }
    }

    private fun submit(index: Int) {
        val current = _state.value as? DailyUiState.Ready ?: return
        if (current.answered) return

        val question = current.question
        val correct = question.isCorrect(index)
        _state.value = current.copy(selectedIndex = index, answered = true, isCorrect = correct)

        viewModelScope.launch {
            val today = timeProvider.todayEpochDay()
            attemptRepository.record(
                Attempt(
                    id = idGenerator.newId(),
                    questionId = question.id,
                    sessionId = null,
                    domain = question.domain,
                    mode = QuizMode.DAILY,
                    selectedIndex = index,
                    isCorrect = correct,
                    answeredAt = timeProvider.nowMillis(),
                ),
            )
            // Streak counts any study activity once per day; StreakCalculator is idempotent for same-day.
            streakRepository.set(StreakCalculator.onActivity(streakRepository.get(), today))
            settingsRepository.setDailyLastAnsweredEpochDay(today)
        }
    }
}
