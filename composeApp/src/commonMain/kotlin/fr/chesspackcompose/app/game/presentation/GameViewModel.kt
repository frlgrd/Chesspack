package fr.chesspackcompose.app.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.chesspackcompose.app.game.domain.Board
import fr.chesspackcompose.app.game.domain.PieceColor
import fr.chesspackcompose.app.game.domain.Promotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    private val board: Board,
    private val boardMapper: BoardMapper
) : ViewModel() {
    private val _state = MutableStateFlow(GameUIState())
    val state = _state.asStateFlow()

    init {
        combine(
            board.piecesFLow,
            board.playerFlow,
            board.takenPiecesFlow
        ) { pieces, player, takenPieces ->
            _state.value = _state.value.copy(
                cells = boardMapper.mapPieces(
                    board = board,
                    pieces = pieces,
                    player = player
                ),
                withesTaken = boardMapper.mapTakenPieces(
                    PieceColor.White,
                    takenPieces
                ),
                blacksTaken = boardMapper.mapTakenPieces(
                    PieceColor.Black,
                    takenPieces
                ),
                promotionUiModel = boardMapper.mapPromotion(board.promotion)
            )
            checkPromotionConsumption(board.promotion, player)
        }.launchIn(viewModelScope)
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
}