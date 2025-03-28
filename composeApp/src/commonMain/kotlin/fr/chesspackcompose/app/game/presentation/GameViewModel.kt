package fr.chesspackcompose.app.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chesspackcompose.composeapp.generated.resources.Res
import fr.chesspackcompose.app.core.audio.SoundEffectPlayer
import fr.chesspackcompose.app.game.domain.Board
import fr.chesspackcompose.app.game.domain.CountdownTimer
import fr.chesspackcompose.app.game.domain.MoveResult
import fr.chesspackcompose.app.game.domain.PieceColor
import fr.chesspackcompose.app.game.domain.defaultTimerDuration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.time.Duration

class GameViewModel(
    private val board: Board,
    private val boardMapper: BoardMapper,
    private val soundEffectPlayer: SoundEffectPlayer,
    whiteCountdownTimer: CountdownTimer,
    blackCountdownTimer: CountdownTimer
) : ViewModel() {
    private val _state = MutableStateFlow(GameUIState())
    val state = _state.asStateFlow()

    private val timers = mapOf(
        PieceColor.White to whiteCountdownTimer,
        PieceColor.Black to blackCountdownTimer,
    )

    init {
        board.state.onEach { boardState ->
            _state.update { ui ->
                ui.copy(
                    cells = boardMapper.mapPieces(boardState),
                    withesGameBanner = boardMapper.mapBanner(
                        color = PieceColor.White,
                        allPieces = boardState.pieces,
                        takenPieces = boardState.takenPieces
                    ),
                    blacksGameBanner = boardMapper.mapBanner(
                        color = PieceColor.Black,
                        allPieces = boardState.pieces,
                        takenPieces = boardState.takenPieces
                    ),
                    promotionUiModel = boardMapper.mapPromotion(boardState.promotion),
                    currentPlayer = boardState.currentPlayer,
                    winner = boardState.winner
                )
            }
            playSoundEffect(moveResult = boardState.moveResult)
            if (boardState.playerSwitched) {
                playerSwitched(currentPlayer = boardState.currentPlayer)
            }
            if (boardState.winner != null) {
                timers.values.forEach(CountdownTimer::pause)
            }
        }.onStart { intTimers() }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        soundEffectPlayer.release()
    }

    fun onEvent(event: GameUiEvent) {
        when (event) {
            is GameUiEvent.PiecePicked -> _state.update {
                val legalMoves = board.legalMoves(event.cell.position)
                it.copy(cells = it.cells.map { cell ->
                    val markAsLegalMove = legalMoves.contains(cell.position)
                    cell.copy(
                        markAsLegalMove = markAsLegalMove,
                        isDragging = cell == event.cell
                    )
                })
            }

            is GameUiEvent.PieceDropped -> board.move(from = event.cell.position, to = event.at)

            is GameUiEvent.DragCanceled -> _state.update {
                it.copy(cells = it.cells.map { cell ->
                    cell.copy(
                        markAsLegalMove = false,
                        isDragging = false,
                        markAsHovered = false
                    )
                })
            }

            is GameUiEvent.PieceDragging -> _state.update {
                it.copy(cells = it.cells.map { cell ->
                    cell.copy(markAsHovered = cell.position == event.at)
                })
            }

            is GameUiEvent.OnPromotion -> board.promote(
                position = event.promotionItem.position,
                color = event.promotionItem.color,
                type = event.promotionItem.type
            )

            is GameUiEvent.ResetRequested -> {
                board.reset()
                intTimers()
            }

            is GameUiEvent.SwitchRotateMode -> _state.update { it.copy(rotateMode = it.rotateMode.switch()) }
        }
    }

    private suspend fun playerSwitched(currentPlayer: PieceColor) {
        timers.values.forEach { it.currentPlayer = currentPlayer }
        timers[currentPlayer.switch()]?.pause()
        delay(300)
        timers[currentPlayer]?.resume()
        _state.update { it.copy(boardRotation = if (it.boardRotation == 180F) 0F else 180F) }
    }

    private fun intTimers() {
        val duration = defaultTimerDuration
        timers.entries.forEach { entry ->
            observeTimer(duration = duration, countdownTimer = entry.value, pieceColor = entry.key)
        }
    }

    private fun observeTimer(
        duration: Duration,
        countdownTimer: CountdownTimer,
        pieceColor: PieceColor
    ) {
        countdownTimer.init(duration)
        countdownTimer.timeLeft.onEach { timeLeft ->
            val timerUi = boardMapper.mapTimer(
                timeLeft = timeLeft,
                timerPlayer = pieceColor,
                currentPlayer = countdownTimer.currentPlayer
            )
            val uiState = when (pieceColor) {
                PieceColor.Black -> _state.value.copy(blackTimer = timerUi)
                PieceColor.White -> _state.value.copy(whiteTimer = timerUi)
            }
            _state.update { uiState }
            if (timeLeft == 0L) {
                board.timeout(pieceColor)
            }
        }.launchIn(viewModelScope)
        if (pieceColor == PieceColor.White) countdownTimer.resume()
    }

    @OptIn(ExperimentalResourceApi::class)
    private fun playSoundEffect(moveResult: MoveResult?) {
        moveResult ?: return
        val effect = when (moveResult) {
            MoveResult.Castling -> "castle"
            MoveResult.SimpleMove -> "move"
            MoveResult.Check -> "check"
            MoveResult.Capture -> "capture"
            MoveResult.Checkmate -> "checkmate"
        }
        soundEffectPlayer.play(uri = Res.getUri("files/${effect}.mp3"))
    }
}