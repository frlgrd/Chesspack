package fr.chesspackcompose.app.game.domain

import fr.chesspackcompose.app.game.domain.pieces.Piece
import kotlinx.coroutines.flow.Flow

interface Board {
    val piecesFLow: Flow<Set<Piece>>
    val player: Flow<PieceColor>
    fun move(from: PiecePosition, to: PiecePosition)
    fun pieceAt(x: Int, y: Int): Piece?
    fun legalMovesFor(x: Int, y: Int): List<PiecePosition>?
}