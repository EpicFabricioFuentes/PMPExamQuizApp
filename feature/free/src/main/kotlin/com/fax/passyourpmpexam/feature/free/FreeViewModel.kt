package com.fax.passyourpmpexam.feature.free

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fax.passyourpmpexam.core.common.IdGenerator
import com.fax.passyourpmpexam.core.common.TimeProvider
import com.fax.passyourpmpexam.core.domain.model.Attempt
import com.fax.passyourpmpexam.core.domain.model.Domain
import com.fax.passyourpmpexam.core.domain.model.Question
import com.fax.passyourpmpexam.core.domain.model.QuizMode
import com.fax.passyourpmpexam.core.domain.repository.AttemptRepository
import com.fax.passyourpmpexam.core.domain.repository.QuestionRepository
import com.fax.passyourpmpexam.core.domain.repository.StreakRepository
import com.fax.passyourpmpexam.core.domain.streak.StreakCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class FreeViewModel(
    private val questionRepository: QuestionRepository,
    private val attemptRepository: AttemptRepository,
    private val streakRepository: StreakRepository,
    private val timeProvider: TimeProvider,
    private val idGenerator: IdGenerator,
    private val random: Random = Random.Default,
) : ViewModel() {

    private val _state = MutableStateFlow(FreeUiState())
    val state: StateFlow<FreeUiState> = _state.asStateFlow()

    private var pool: List<Question> = emptyList()
    private val queue = ArrayDeque<Question>()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                pool = questionRepository.getAll()
                queue.clear()
                refillQueue()
                _state.value = _state.value.copy(
                    loading = false,
                    question = nextFromQueue(),
                    error = null,
                )
            } catch (t: Throwable) {
                _state.value = _state.value.copy(loading = false, error = LOAD_ERROR_MESSAGE)
            }
        }
    }

    fun onIntent(intent: FreeIntent) {
        when (intent) {
            is FreeIntent.ToggleDomain -> toggleDomain(intent.domain)
            is FreeIntent.SelectOption -> selectOption(intent.index)
            FreeIntent.Next -> advance()
            FreeIntent.Retry -> load()
        }
    }

    private fun toggleDomain(domain: Domain) {
        val current = _state.value.selectedDomains
        val updated = if (domain in current) current - domain else current + domain
        queue.clear()
        refillQueue(updated)
        _state.value = _state.value.copy(
            selectedDomains = updated,
            question = nextFromQueue(),
            selectedIndex = null,
            answered = false,
            isCorrect = null,
        )
    }

    private fun selectOption(index: Int) {
        val state = _state.value
        val question = state.question ?: return
        if (state.answered) return

        val correct = question.isCorrect(index)
        _state.value = state.copy(selectedIndex = index, answered = true, isCorrect = correct)

        viewModelScope.launch {
            attemptRepository.record(
                Attempt(
                    id = idGenerator.newId(),
                    questionId = question.id,
                    sessionId = null,
                    domain = question.domain,
                    mode = QuizMode.FREE,
                    selectedIndex = index,
                    isCorrect = correct,
                    answeredAt = timeProvider.nowMillis(),
                ),
            )
            streakRepository.set(
                StreakCalculator.onActivity(streakRepository.get(), timeProvider.todayEpochDay()),
            )
        }
    }

    private fun advance() {
        _state.value = _state.value.copy(
            question = nextFromQueue(),
            selectedIndex = null,
            answered = false,
            isCorrect = null,
        )
    }

    private fun filteredPool(domains: Set<Domain> = _state.value.selectedDomains): List<Question> =
        if (domains.isEmpty()) pool else pool.filter { it.domain in domains }

    private fun refillQueue(domains: Set<Domain> = _state.value.selectedDomains) {
        queue.addAll(filteredPool(domains).shuffled(random))
    }

    /** Pops the next question, reshuffling the filtered pool when the queue runs dry (unlimited practice). */
    private fun nextFromQueue(): Question? {
        if (queue.isEmpty()) refillQueue()
        return queue.removeFirstOrNull()
    }

    private companion object {
        const val LOAD_ERROR_MESSAGE = "We couldn't load practice questions. Please try again."
    }
}
