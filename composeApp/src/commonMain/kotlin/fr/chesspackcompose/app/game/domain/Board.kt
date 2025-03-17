package fr.chesspackcompose.app.game.domain

import fr.chesspackcompose.app.game.domain.pieces.Piece
import kotlinx.coroutines.flow.Flow

interface Board {
    val pieces: Flow<Set<Piece>>
    fun move(from: PiecePosition, to: PiecePosition)
    fun pieceAt(x: Int, y: Int): Piece?
    fun legalMovesFor(x: Int, y: Int): List<PiecePosition>
}