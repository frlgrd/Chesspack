package fr.chesspackcompose.app.game.domain

import fr.chesspackcompose.app.game.domain.pieces.Piece
import kotlinx.coroutines.flow.Flow

interface Board {
    val piecesFLow: Flow<Set<Piece>>
    val playerFlow: Flow<PieceColor>
    val moveResult: Flow<MoveResult?>
    val takenPiecesFlow: Flow<Map<PieceColor, List<Piece>>>
    var promotion: Promotion?
    val winner: PieceColor?
    fun move(from: PiecePosition, to: PiecePosition)
    fun pieceAt(pieces: Set<Piece>, x: Int, y: Int): Piece?
    fun legalMoves(position: PiecePosition): List<PiecePosition>
    fun promote(position: PiecePosition, color: PieceColor, type: Promotion.Type)
}