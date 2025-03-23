package fr.chesspackcompose.app.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chesspackcompose.composeapp.generated.resources.Res
import fr.chesspackcompose.app.core.audio.SoundEffectPlayer
import fr.chesspackcompose.app.game.domain.Board
import fr.chesspackcompose.app.game.domain.MoveResult
import fr.chesspackcompose.app.game.domain.PieceColor
import fr.chesspackcompose.app.game.domain.Promotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
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
        combine(
            board.piecesFLow,
            board.playerFlow,
            board.takenPiecesFlow,
            board.moveResult,
        ) { pieces, player, takenPieces, moveResult ->
            _state.value = _state.value.copy(
                cells = boardMapper.mapPieces(
                    board = board,
                    pieces = pieces,
                    player = player
                ),
                withesGameInfo = boardMapper.mapGameInfo(
                    color = PieceColor.White,
                    allPieces = pieces,
                    takenPieces = takenPieces
                ),
                blacksGameInfo = boardMapper.mapGameInfo(
                    color = PieceColor.Black,
                    allPieces = pieces,
                    takenPieces = takenPieces
                ),
                promotionUiModel = boardMapper.mapPromotion(board.promotion),
                currentPlayer = player
            )
            checkPromotionConsumption(board.promotion, player)
            playSoundEffect(moveResult)
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
                    board.move(from = event.cell.position, to = event.at)
                    _state.update { it.copy(canReset = true) }
                    if (board.winner == null && board.promotion == null) {
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

    private fun checkPromotionConsumption(promotion: Promotion?, player: PieceColor) {
        promotion ?: return
        if (promotion.pawn.color == player) return
        board.promotion = null
        _state.update { it.copy(promotionUiModel = null) }
        rotateBoard()
    }

    private fun rotateBoard() {
        _state.update { it.copy(boardRotation = if (it.boardRotation == 180F) 0F else 180F) }
    }

    @OptIn(ExperimentalResourceApi::class)
    private fun playSoundEffect(moveResult: MoveResult?) {
        val soundEffectFileName = when (moveResult) {
            MoveResult.Castling -> "castle"
            MoveResult.SimpleMove -> "move"
            MoveResult.Check -> "check"
            MoveResult.Capture -> "capture"
            MoveResult.Checkmate -> "checkmate"
            null -> null
        } ?: return

        val uri = Res.getUri("files/${soundEffectFileName}.mp3")
        soundEffectPlayer.play(uri = uri)
    }
}