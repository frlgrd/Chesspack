package fr.chesspackcompose.app.game.data

import fr.chesspackcompose.app.game.domain.CountdownTimer
import fr.chesspackcompose.app.game.domain.PieceColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration

class CountdownTimerImpl : CountdownTimer {

    private val _timeLeft = MutableStateFlow(0L)
    override val timeLeft: Flow<Long> get() = _timeLeft.asStateFlow()
    override var currentPlayer: PieceColor = PieceColor.White
    private var onGoing = false

    companion object {
        private const val SECOND_STEP = 1000L
    }

    private var finished = false
    private var step = SECOND_STEP

    private var timerJob: Job? = null

    override fun init(duration: Duration) {
        _timeLeft.value = duration.inWholeMilliseconds
        finished = false
        step = SECOND_STEP
        if (timerJob != null) timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.Default).launch { while (!finished) tick() }
    }

    override fun pause() {
        onGoing = false
    }

    override fun resume() {
        onGoing = true
    }

    private suspend fun tick() {
        delay(step)
        if (!onGoing) return
        _timeLeft.update { it - step }
        val timeLeft = _timeLeft.value
        if (timeLeft - step < 0L) {
            finished = true
        }
        if (timeLeft < 10 * SECOND_STEP && step == SECOND_STEP) {
            step = 100L
        }
    }
}