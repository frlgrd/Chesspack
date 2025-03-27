package fr.chesspackcompose.app.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chesspackcompose.composeapp.generated.resources.Res
import fr.chesspackcompose.app.core.audio.SoundEffectPlayer
import fr.chesspackcompose.app.game.domain.Board
import fr.chesspackcompose.app.game.domain.MoveResult
import fr.chesspackcompose.app.game.domain.PieceColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.ExperimentalResourceApi

class GameViewModel(
    private val board: Board,
    private val boardMapper: BoardMapper,
    private val soundEffectPlayer: SoundEffectPlayer
) : ViewModel() {
    private val _state = MutableStateFlow(GameUIState())
    val state = _state.asStateFlow()

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
                playerSwitched()
            }
        }.launchIn(viewModelScope)
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

            is GameUiEvent.ResetRequested -> board.reset()
            is GameUiEvent.TimerFinished -> {}
            is GameUiEvent.SwitchRotateMode -> _state.update { it.copy(rotateMode = it.rotateMode.switch()) }
        }
    }

    private suspend fun playerSwitched() {
        delay(300)
        _state.update { it.copy(boardRotation = if (it.boardRotation == 180F) 0F else 180F) }
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