package fr.chesspackcompose.app.game.domain

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

val defaultTimerDuration = 10.minutes

interface CountdownTimer {
    val timeLeft: Flow<Long>
    var currentPlayer: PieceColor
    fun init(duration: Duration)
    fun pause()
    fun resume()
}