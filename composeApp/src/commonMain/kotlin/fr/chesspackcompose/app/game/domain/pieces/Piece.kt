package fr.chesspackcompose.app.game.domain.pieces

import fr.chesspackcompose.app.game.domain.PieceColor
import fr.chesspackcompose.app.game.domain.PiecePosition

sealed class Piece(
    open var position: PiecePosition,
    open val color: PieceColor,
    val power: Int
) {
    var moved = false
        private set

    fun markAsMoved() {
        moved = true
    }
}
