package fr.chesspackcompose.app.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.chesspackcompose.app.game.domain.Board
import fr.chesspackcompose.app.game.domain.PieceColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    private val board: Board,
    private val piecesMapper: PiecesMapper
) : ViewModel() {
    private val _state = MutableStateFlow(GameUIState())
    val state = _state.asStateFlow()

    init {
        combine(
            board.piecesFLow,
            board.player,
            board.takenPieces,
            board.winner
        ) { pieces, player, takenPieces, winner ->
            _state.update {
                it.copy(
                    cells = piecesMapper.mapPieces(
                        board = board,
                        pieces = pieces,
                        player = player
                    ),
                    withesTaken = piecesMapper.mapTakenPieces(
                        PieceColor.White,
                        takenPieces
                    ),
                    blacksTaken = piecesMapper.mapTakenPieces(
                        PieceColor.Black,
                        takenPieces
                    ),
                    winner = winner
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: GameUiEvent) {
        when (event) {
            is GameUiEvent.PiecePicked -> _state.update {
                val legalMoves = board.legalMoves(event.cell.position)
                it.copy(cells = it.cells.map { cell ->
                    val markAsLegalMove = legalMoves?.contains(cell.position) == true
                    cell.copy(
                        markAsLegalMove = markAsLegalMove,
                        isDragging = cell == event.cell
                    )
                })
            }

            is GameUiEvent.PieceDropped -> {
                viewModelScope.launch {
                    board.move(from = event.cell.position, to = event.at)
                    delay(300)
                    _state.update { it.copy(boardRotation = if (it.boardRotation == 180F) 0F else 180F) }
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
        }
    }
}