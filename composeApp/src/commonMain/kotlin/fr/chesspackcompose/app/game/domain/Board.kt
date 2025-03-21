package fr.chesspackcompose.app.game.domain

import fr.chesspackcompose.app.game.domain.pieces.Piece
import kotlinx.coroutines.flow.Flow

interface Board {
    val piecesFLow: Flow<Set<Piece>>
    val playerFlow: Flow<PieceColor>
    val takenPiecesFlow: Flow<Map<PieceColor, List<Piece>>>
    val winner: PieceColor?
    fun move(from: PiecePosition, to: PiecePosition)
    fun pieceAt(pieces: Set<Piece>, x: Int, y: Int): Piece?
    fun legalMoves(piecePosition: PiecePosition): List<PiecePosition>
}