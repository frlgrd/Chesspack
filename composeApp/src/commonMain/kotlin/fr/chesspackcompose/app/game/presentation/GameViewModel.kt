package fr.chesspackcompose.app.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.chesspackcompose.app.game.domain.Board
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class GameViewModel(
    private val board: Board,
    private val piecesMapper: PiecesMapper
) : ViewModel() {
    private val _state = MutableStateFlow(GameUIState())
    val state = _state.asStateFlow()

    init {
        board.piecesFLow
            .map { piecesMapper.map(board, it) }
            .onEach { cells -> _state.update { it.copy(cells = cells) } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: GameUiEvent) {
        when (event) {
            is GameUiEvent.PiecePicked -> _state.update {
                val legalMoves = board.legalMovesFor(event.cell.position.x, event.cell.position.y)
                it.copy(cells = it.cells.map { cell ->
                    val markAsLegalMove = legalMoves?.contains(cell.position) == true
                    cell.copy(
                        markAsLegalMove = markAsLegalMove,
                        isDragging = cell == event.cell
                    )
                })
            }

            is GameUiEvent.PieceDropped -> {
                board.move(from = event.cell.position, to = event.droppedAt)
            }

            is GameUiEvent.DragCanceled -> _state.update {
                it.copy(cells = it.cells.map { cell ->
                    cell.copy(
                        markAsLegalMove = false,
                        isDragging = false
                    )
                })
            }
        }
    }
}