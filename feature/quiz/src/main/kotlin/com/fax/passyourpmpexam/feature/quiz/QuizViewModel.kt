package com.fax.passyourpmpexam.feature.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fax.passyourpmpexam.core.common.IdGenerator
import com.fax.passyourpmpexam.core.common.TimeProvider
import com.fax.passyourpmpexam.core.domain.model.Attempt
import com.fax.passyourpmpexam.core.domain.model.Question
import com.fax.passyourpmpexam.core.domain.model.QuizConfig
import com.fax.passyourpmpexam.core.domain.model.QuizMode
import com.fax.passyourpmpexam.core.domain.model.QuizSession
import com.fax.passyourpmpexam.core.domain.model.QuizStatus
import com.fax.passyourpmpexam.core.domain.model.QuizType
import com.fax.passyourpmpexam.core.domain.model.SessionQuestion
import com.fax.passyourpmpexam.core.domain.repository.AttemptRepository
import com.fax.passyourpmpexam.core.domain.repository.QuestionRepository
import com.fax.passyourpmpexam.core.domain.repository.QuizSessionRepository
import com.fax.passyourpmpexam.core.domain.repository.StreakRepository
import com.fax.passyourpmpexam.core.domain.scoring.Scorer
import com.fax.passyourpmpexam.core.domain.selection.QuestionSelector
import com.fax.passyourpmpexam.core.domain.streak.StreakCalculator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class QuizViewModel(
    private val questionRepository: QuestionRepository,
    private val attemptRepository: AttemptRepository,
    private val quizSessionRepository: QuizSessionRepository,
    private val streakRepository: StreakRepository,
    private val timeProvider: TimeProvider,
    private val idGenerator: IdGenerator,
    private val savedStateHandle: SavedStateHandle,
    private val random: Random = Random.Default,
) : ViewModel() {

    private val _state = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    private var pool: List<Question> = emptyList()
    private var timerJob: Job? = null

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = QuizUiState.Loading
            try {
                pool = questionRepository.getAll()
                if (pool.isEmpty()) {
                    _state.value = QuizUiState.Empty
                    return@launch
                }
                // Same-process restore (config change / VM recreation) via SavedStateHandle.
                val savedId = savedStateHandle.get<String>(KEY_SESSION_ID)
                val restored = savedId?.let { quizSessionRepository.getById(it) }
                if (restored != null && restored.status == QuizStatus.IN_PROGRESS) {
                    enterInProgress(restored)
                    return@launch
                }
                // Cold start after process death: offer to resume a persisted in-progress session.
                val resumable = quizSessionRepository.getLatestByStatus(QuizStatus.IN_PROGRESS)
                _state.value =
                    if (resumable != null) QuizUiState.ResumePrompt(resumable) else QuizUiState.Setup(DEFAULT_TYPE)
            } catch (t: Throwable) {
                _state.value = QuizUiState.Error(LOAD_ERROR_MESSAGE)
            }
        }
    }

    fun onIntent(intent: QuizIntent) {
        when (intent) {
            is QuizIntent.SelectType -> selectType(intent.type)
            QuizIntent.Start -> start()
            QuizIntent.ResumeSaved -> resumeSaved()
            QuizIntent.DiscardSaved -> discardSaved()
            is QuizIntent.SelectOption -> selectOption(intent.index)
            QuizIntent.Next -> move(delta = 1)
            QuizIntent.Previous -> move(delta = -1)
            QuizIntent.Submit -> submit()
            QuizIntent.Restart -> restart()
            QuizIntent.ExitToSetup -> exitToSetup()
            QuizIntent.Retry -> load()
        }
    }

    /** Called by the screen on lifecycle ON_START — resume ticking after returning to the foreground. */
    fun onForeground() {
        if (_state.value is QuizUiState.InProgress) startTimer()
    }

    /** Called by the screen on lifecycle ON_STOP — pause the timer and persist progress. */
    fun onBackground() {
        timerJob?.cancel()
        timerJob = null
        (_state.value as? QuizUiState.InProgress)?.let { persist(it.session) }
    }

    private fun selectType(type: QuizType) {
        val setup = _state.value as? QuizUiState.Setup ?: return
        _state.value = setup.copy(selectedType = type)
    }

    private fun start() {
        val setup = _state.value as? QuizUiState.Setup ?: return
        val config = QuizConfig(setup.selectedType)
        val questions = QuestionSelector.select(pool, config, random).map { SessionQuestion(it) }
        val session = QuizSession(
            id = idGenerator.newId(),
            config = config,
            questions = questions,
            createdAt = timeProvider.nowMillis(),
        )
        persist(session)
        enterInProgress(session)
    }

    private fun resumeSaved() {
        val prompt = _state.value as? QuizUiState.ResumePrompt ?: return
        enterInProgress(prompt.session)
    }

    private fun discardSaved() {
        val prompt = _state.value as? QuizUiState.ResumePrompt ?: return
        persist(prompt.session.copy(status = QuizStatus.ABANDONED))
        savedStateHandle[KEY_SESSION_ID] = null
        _state.value = QuizUiState.Setup(DEFAULT_TYPE)
    }

    private fun enterInProgress(session: QuizSession) {
        savedStateHandle[KEY_SESSION_ID] = session.id
        _state.value = QuizUiState.InProgress(session = session, remainingMillis = remainingFor(session))
        startTimer()
    }

    private fun selectOption(index: Int) {
        val inProgress = _state.value as? QuizUiState.InProgress ?: return
        val questions = inProgress.session.questions.toMutableList()
        val i = inProgress.session.currentIndex
        questions[i] = questions[i].copy(selectedIndex = index)
        val updated = inProgress.session.copy(questions = questions)
        _state.value = inProgress.copy(session = updated)
        persist(updated)
    }

    private fun move(delta: Int) {
        val inProgress = _state.value as? QuizUiState.InProgress ?: return
        val newIndex = (inProgress.session.currentIndex + delta)
            .coerceIn(0, inProgress.session.questions.lastIndex)
        val updated = inProgress.session.copy(currentIndex = newIndex)
        _state.value = inProgress.copy(session = updated)
        persist(updated)
    }

    private fun submit() {
        val inProgress = _state.value as? QuizUiState.InProgress ?: return
        timerJob?.cancel()
        timerJob = null

        val completed = inProgress.session.copy(
            status = QuizStatus.COMPLETED,
            completedAt = timeProvider.nowMillis(),
        )
        val result = Scorer.score(completed)
        _state.value = QuizUiState.Results(result = result, session = completed)
        savedStateHandle[KEY_SESSION_ID] = null

        viewModelScope.launch {
            quizSessionRepository.save(completed)
            val answeredAt = timeProvider.nowMillis()
            val attempts = completed.questions
                .filter { it.selectedIndex != null }
                .map { slot ->
                    Attempt(
                        id = idGenerator.newId(),
                        questionId = slot.question.id,
                        sessionId = completed.id,
                        domain = slot.question.domain,
                        mode = QuizMode.QUIZ,
                        selectedIndex = slot.selectedIndex!!,
                        isCorrect = slot.isCorrect == true,
                        answeredAt = answeredAt,
                    )
                }
            attemptRepository.recordAll(attempts)
            streakRepository.set(
                StreakCalculator.onActivity(streakRepository.get(), timeProvider.todayEpochDay()),
            )
        }
    }

    private fun restart() {
        timerJob?.cancel()
        timerJob = null
        savedStateHandle[KEY_SESSION_ID] = null
        _state.value = QuizUiState.Setup(DEFAULT_TYPE)
    }

    /** Back arrow while answering: abandon the current session (so it won't resurface as a resume
     *  prompt) and return to setup with the same quiz type still selected. */
    private fun exitToSetup() {
        val inProgress = _state.value as? QuizUiState.InProgress ?: return
        timerJob?.cancel()
        timerJob = null
        persist(inProgress.session.copy(status = QuizStatus.ABANDONED))
        savedStateHandle[KEY_SESSION_ID] = null
        _state.value = QuizUiState.Setup(inProgress.session.config.type)
    }

    /** Accrues elapsed time only while running; the screen stops/starts it on background/foreground. */
    private fun startTimer() {
        val limit = (_state.value as? QuizUiState.InProgress)
            ?.session?.config?.type?.timeLimitMillis ?: return
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val current = _state.value as? QuizUiState.InProgress ?: return@launch
                if (current.session.elapsedMillis >= limit) {
                    submit()
                    return@launch
                }
                delay(TICK_MILLIS)
                val running = _state.value as? QuizUiState.InProgress ?: return@launch
                val newElapsed = running.session.elapsedMillis + TICK_MILLIS
                _state.value = running.copy(
                    session = running.session.copy(elapsedMillis = newElapsed),
                    remainingMillis = (limit - newElapsed).coerceAtLeast(0L),
                )
            }
        }
    }

    private fun remainingFor(session: QuizSession): Long? =
        session.config.type.timeLimitMillis?.let { (it - session.elapsedMillis).coerceAtLeast(0L) }

    private fun persist(session: QuizSession) {
        viewModelScope.launch { quizSessionRepository.save(session) }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    private companion object {
        val DEFAULT_TYPE = QuizType.SHORT_10
        const val TICK_MILLIS = 1_000L
        const val KEY_SESSION_ID = "quiz_session_id"
        const val LOAD_ERROR_MESSAGE = "We couldn't load your quiz. Please try again."
    }
}
