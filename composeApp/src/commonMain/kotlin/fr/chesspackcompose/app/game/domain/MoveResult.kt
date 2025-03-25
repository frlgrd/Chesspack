package fr.chesspackcompose.app.game.domain

sealed interface MoveResult {
    data object SimpleMove : MoveResult
    data object Capture : MoveResult
    data object Check : MoveResult
    data object Checkmate : MoveResult
    data object Castling : MoveResult
}