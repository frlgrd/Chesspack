package fr.chesspackcompose.app.game.domain.pieces

import fr.chesspackcompose.app.game.domain.PieceColor
import fr.chesspackcompose.app.game.domain.PiecePosition

sealed class Piece(
    open var position: PiecePosition,
    open val color: PieceColor,
    val power: Int
) {
    var legalMoves: List<PiecePosition> = emptyList()
        private set
    var moved = false
        private set

    fun updateLegalMoves(moves: List<PiecePosition>): Piece {
        legalMoves = moves
        return this
    }

    fun markAsMoved() {
        moved = true
    }

    fun copyPiece() = when (this) {
        is Bishop -> copy()
        is King -> copy()
        is Knight -> copy()
        is Pawn -> copy()
        is Queen -> copy()
        is Rook -> copy()
    }
}
