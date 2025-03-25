package fr.chesspackcompose.app.game.domain

import fr.chesspackcompose.app.game.domain.pieces.Piece
import kotlinx.coroutines.flow.Flow

interface Board {
    val state: Flow<BoardState>
    fun move(from: PiecePosition, to: PiecePosition)
    fun pieceAt(pieces: Set<Piece>, x: Int, y: Int): Piece?
    fun legalMoves(position: PiecePosition): List<PiecePosition>
    fun promote(position: PiecePosition, color: PieceColor, type: Promotion.Type)
    fun reset()
}