package fr.chesspackcompose.app.game.domain

import fr.chesspackcompose.app.game.domain.pieces.Piece
import kotlinx.coroutines.flow.Flow

interface Board {
    val state: Flow<State>
    fun move(from: PiecePosition, to: PiecePosition)
    fun legalMoves(position: PiecePosition): List<PiecePosition>
    fun promote(position: PiecePosition, color: PieceColor, type: Promotion.Type)
    fun reset()

    data class State(
        val pieces: MutableSet<Piece> = mutableSetOf(),
        val currentPlayer: PieceColor = PieceColor.White,
        val moveResult: MoveResult? = null,
        val takenPieces: Map<PieceColor, MutableList<Piece>> = emptyMap(),
        val promotion: Promotion? = null,
        val winner: PieceColor? = null,
        var playerSwitched: Boolean = false
    )
}