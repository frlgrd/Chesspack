package fr.chesspackcompose.app.game.domain

import fr.chesspackcompose.app.game.domain.pieces.Piece
import kotlinx.coroutines.flow.Flow

interface Board {
    val piecesFLow: Flow<Set<Piece>>
    val player: Flow<PieceColor>
    val takenPieces: Flow<Map<PieceColor, List<Piece>>>
    val winner: Flow<PieceColor?>
    fun move(from: PiecePosition, to: PiecePosition)
    fun pieceAt(pieces: Set<Piece>, x: Int, y: Int): Piece?
    fun legalMoves(piecePosition: PiecePosition): List<PiecePosition>
}