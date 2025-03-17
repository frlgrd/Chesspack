package fr.chesspackcompose.app.game.data

import fr.chesspackcompose.app.game.domain.Board
import fr.chesspackcompose.app.game.domain.PieceColor
import fr.chesspackcompose.app.game.domain.PiecePosition
import fr.chesspackcompose.app.game.domain.pieces.Bishop
import fr.chesspackcompose.app.game.domain.pieces.King
import fr.chesspackcompose.app.game.domain.pieces.Knight
import fr.chesspackcompose.app.game.domain.pieces.Pawn
import fr.chesspackcompose.app.game.domain.pieces.Piece
import fr.chesspackcompose.app.game.domain.pieces.Queen
import fr.chesspackcompose.app.game.domain.pieces.Rook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BoardImpl(
    private val fen: Fen = Fen()
) : Board {

    private val _pieces: MutableStateFlow<Set<Piece>> = MutableStateFlow(mutableSetOf())
    override val pieces: Flow<Set<Piece>> get() = _pieces.asStateFlow()

    init {
        val pieces = mutableSetOf<Piece>()
        (0..7).forEach { x ->
            (0..7).forEach { y ->
                fen.pieceAt(x, y)?.let { pieces.add(it) }
            }
        }
        _pieces.value = pieces
    }

    override fun move(from: PiecePosition, to: PiecePosition) {
        if (from == to) return
        val pieces = _pieces.value.toMutableSet()
        val piece = pieces.find { it.position == from } ?: return
        val target = pieces.find { it.position == to }
        if (target?.color == piece.color) {
            castling(piece, target)
        } else {
            pieces.removeAll { it.position == to }
            piece.position = to
        }
        piece.markAsMoved()
        updateCheckedKings(pieces)
        _pieces.value = pieces
    }

    override fun pieceAt(x: Int, y: Int): Piece? {
        return _pieces.value.find { it.position.x == x && it.position.y == y }
    }

    override fun legalMovesFor(x: Int, y: Int): List<PiecePosition>? {
        val piece = pieceAt(x = x, y = y) ?: return null
        return legalMoves(piece)
    }

    private fun updateCheckedKings(pieces: MutableSet<Piece>) {
        pieces
            .filterIsInstance<King>()
            .map { king ->
                val isChecked = opponentsMoves(pieces, king).contains(king.position)
                king.updateCheck(isChecked = isChecked)
            }
    }

    private fun canCastling(king: King, rook: Rook): Boolean {
        if (king.moved || rook.moved) return false
        val range = if (king.position.x < rook.position.x) {
            king.position.x + 1..<rook.position.x // pieces between king and east rook
        } else {
            king.position.x - 1 downTo rook.position.x + 1 // pieces between king and west rook
        }
        val piecesBetween = range.mapNotNull { pieceAt(x = it, y = king.position.y) }
        return piecesBetween.isEmpty()
    }

    private fun castling(king: Piece, rook: Piece) {
        if (king !is King) return // Should not happen
        if (rook !is Rook) return // Should not happen
        if (king.position.x < rook.position.x) {
            king.position = king.position.copy(x = king.position.x + 2)
            rook.position = rook.position.copy(x = king.position.x - 1)
        } else {
            king.position = king.position.copy(x = king.position.x - 2)
            rook.position = rook.position.copy(x = king.position.x + 1)
        }
    }

    private fun opponentsMoves(pieces: Set<Piece>, piece: Piece): List<PiecePosition> {
        return pieces.filter { it.color != piece.color }
            .map(::legalMoves)
            .flatten()
            .distinct()
    }

    // region Moves
    private fun legalMoves(piece: Piece): List<PiecePosition> = when (piece) {
        is Bishop -> diagonalMoves(piece = piece)
        is King -> kingMoves(king = piece)
        is Knight -> knightMoves(piece = piece)
        is Pawn -> pawnMoves(pawn = piece)
        is Queen -> straightMoves(piece = piece) + diagonalMoves(piece = piece)
        is Rook -> straightMoves(piece = piece)
    }

    private fun kingMoves(king: King): List<PiecePosition> {
        return diagonalMoves(piece = king, max = 1) +
                straightMoves(piece = king, max = 1) +
                castlingMoves(king = king)
    }

    private fun knightMoves(piece: Piece): List<PiecePosition> = listOfNotNull(
        jumpOverMove(piece = piece, xDirection = -1, yDirection = -2),
        jumpOverMove(piece = piece, xDirection = 1, yDirection = -2),
        jumpOverMove(piece = piece, xDirection = -1, yDirection = 2),
        jumpOverMove(piece = piece, xDirection = 1, yDirection = 2),
        jumpOverMove(piece = piece, xDirection = -2, yDirection = -1),
        jumpOverMove(piece = piece, xDirection = 2, yDirection = -1),
        jumpOverMove(piece = piece, xDirection = -2, yDirection = 1),
        jumpOverMove(piece = piece, xDirection = 2, yDirection = 1),
    )

    private fun straightMoves(piece: Piece, max: Int = 7): List<PiecePosition> {
        return searchMove(piece = piece, xDirection = -1, yDirection = 0, max = max) +
                searchMove(piece = piece, xDirection = 0, yDirection = -1, max = max) +
                searchMove(piece = piece, xDirection = 1, yDirection = 0, max = max) +
                searchMove(piece = piece, xDirection = 0, yDirection = 1, max = max)
    }

    private fun diagonalMoves(piece: Piece, max: Int = 7): List<PiecePosition> {
        return searchMove(piece = piece, xDirection = 1, yDirection = 1, max = max) +
                searchMove(piece = piece, xDirection = -1, yDirection = 1, max = max) +
                searchMove(piece = piece, xDirection = -1, yDirection = -1, max = max) +
                searchMove(piece = piece, xDirection = 1, yDirection = -1, max = max)
    }

    private fun jumpOverMove(piece: Piece, xDirection: Int, yDirection: Int): PiecePosition? {
        val x = piece.position.x + xDirection
        val y = piece.position.y + yDirection
        if (x == -1 || y == -1 || x == 8 || y == 8) return null
        val pieceAt = pieceAt(x = x, y = y)
        if (pieceAt != null && pieceAt.color == piece.color) return null
        return PiecePosition(x = x, y = y)
    }

    private fun pawnMoves(pawn: Pawn): List<PiecePosition> {
        val direction = if (pawn.color == PieceColor.White) -1 else 1
        return searchMove(
            piece = pawn,
            xDirection = 0,
            yDirection = direction,
            max = if (pawn.moved) 1 else 2,
            canAttackFromFront = false
        ) + pawnAttackMoves(piece = pawn, direction = direction)
    }

    private fun pawnAttackMoves(
        piece: Piece,
        direction: Int
    ): List<PiecePosition> {
        val attackMoves = mutableListOf<PiecePosition>()
        val leftPiece = pieceAt(x = piece.position.x - 1, y = piece.position.y + direction)
        if (leftPiece != null && leftPiece.color != piece.color) {
            attackMoves.add(leftPiece.position)
        }
        val rightPiece = pieceAt(x = piece.position.x + 1, y = piece.position.y + direction)
        if (rightPiece != null && rightPiece.color != piece.color) {
            attackMoves.add(rightPiece.position)
        }
        return attackMoves
    }

    private fun castlingMoves(king: King): List<PiecePosition> {
        val rookForKing = _pieces.value
            .filterIsInstance<Rook>()
            .filter { it.color == king.color }
        return rookForKing.mapNotNull { rook ->
            if (canCastling(king, rook)) rook.position else null
        }
    }

    private fun searchMove(
        piece: Piece, xDirection: Int,
        yDirection: Int,
        max: Int,
        canAttackFromFront: Boolean = true
    ): List<PiecePosition> {
        val moves = mutableListOf<PiecePosition>()
        var x = piece.position.x
        var y = piece.position.y
        var move = 0
        do {
            x += xDirection
            y += yDirection
            if (x == -1 || y == -1 || x == 8 || y == 8) break
            val pieceAt = pieceAt(x = x, y = y)
            if (pieceAt != null && pieceAt.color == piece.color) break
            val enemyMet = pieceAt != null && pieceAt.color != piece.color
            if (enemyMet && !canAttackFromFront) break
            moves.add(PiecePosition(x = x, y = y))
            move++
        } while (!enemyMet && move < max)
        return moves
    }
    // endregion moves
}