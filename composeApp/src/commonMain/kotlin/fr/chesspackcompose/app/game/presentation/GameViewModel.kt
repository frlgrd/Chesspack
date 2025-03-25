package fr.chesspackcompose.app.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chesspackcompose.composeapp.generated.resources.Res
import fr.chesspackcompose.app.core.audio.SoundEffectPlayer
import fr.chesspackcompose.app.game.domain.Board
import fr.chesspackcompose.app.game.domain.BoardState
import fr.chesspackcompose.app.game.domain.PieceColor
import fr.chesspackcompose.app.game.domain.SoundEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
            println(boardState.pieces.map { it.legalMoves })
            _state.update {
                it.copy(
                    cells = boardMapper.mapPieces(boardState),
                    withesGameInfo = boardMapper.mapGameInfo(
                        color = PieceColor.White,
                        allPieces = boardState.pieces,
                        takenPieces = boardState.takenPieces
                    ),
                    blacksGameInfo = boardMapper.mapGameInfo(
                        color = PieceColor.Black,
                        allPieces = boardState.pieces,
                        takenPieces = boardState.takenPieces
                    ),
                    promotionUiModel = boardMapper.mapPromotion(boardState.promotion),
                    currentPlayer = boardState.currentPlayer,
                    winner = boardState.winner
                )
            }
            checkPromotionConsumption(boardState = boardState)
            playSoundEffect(soundEffect = boardState.soundEffect)
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

            is GameUiEvent.PieceDropped -> {
                viewModelScope.launch {
                    val state = board.move(from = event.cell.position, to = event.at)
                    _state.update { it.copy(canReset = true) }
                    if (state.winner == null && state.promotion == null) {
                        delay(300)
                        rotateBoard()
                    }
                }
            }

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
                _state.update { it.copy(boardRotation = 0F, canReset = false) }
            }

            is GameUiEvent.TimerFinished -> {}
        }
    }

    private fun checkPromotionConsumption(boardState: BoardState) {
        boardState.promotion ?: return
        if (boardState.promotion.pawn.color == boardState.currentPlayer) return
        _state.update { it.copy(promotionUiModel = null) }
        board.promotionConsumed()
        rotateBoard()
    }

    private fun rotateBoard() {
        _state.update { it.copy(boardRotation = if (it.boardRotation == 180F) 0F else 180F) }
    }

    @OptIn(ExperimentalResourceApi::class)
    private fun playSoundEffect(soundEffect: SoundEffect?) {
        soundEffect ?: return
        val effect = when (soundEffect) {
            SoundEffect.Castling -> "castle"
            SoundEffect.SimpleMove -> "move"
            SoundEffect.Check -> "check"
            SoundEffect.Capture -> "capture"
            SoundEffect.Checkmate -> "checkmate"
        }
        soundEffectPlayer.play(uri = Res.getUri("files/${effect}.mp3"))
    }
}