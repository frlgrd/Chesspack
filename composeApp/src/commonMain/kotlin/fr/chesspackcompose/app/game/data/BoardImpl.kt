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
import kotlinx.coroutines.flow.update

class BoardImpl(
    fen: Fen
) : Board {

    private val _piecesFlow: MutableStateFlow<MutableSet<Piece>> = MutableStateFlow(mutableSetOf())
    private val _player: MutableStateFlow<PieceColor> = MutableStateFlow(PieceColor.White)

    override val piecesFLow: Flow<Set<Piece>> get() = _piecesFlow.asStateFlow()
    override val player: Flow<PieceColor> get() = _player.asStateFlow()


    init {
        sendUpdate(pieces = fen.toPieces())
    }

    override fun move(from: PiecePosition, to: PiecePosition) {
        val pieces = _piecesFlow.value
        val piece = pieces.find { it.position == from } ?: return
        val target = pieces.find { it.position == to }
        if (target?.color == piece.color) {
            castling(king = piece, rook = target)
        } else {
            movePiece(piece = piece, to = to)
        }
        piece.markAsMoved()
        updateCheckedKings()
        switchPlayer()
        sendUpdate(pieces = pieces)
    }

    private fun switchPlayer() {
        _player.value = when (_player.value) {
            PieceColor.Black -> PieceColor.White
            PieceColor.White -> PieceColor.Black
        }
    }

    private fun movePiece(piece: Piece, to: PiecePosition) {
        _piecesFlow.value.removeAll { it.position == to }
        piece.position = to
    }

    override fun pieceAt(x: Int, y: Int): Piece? {
        return _piecesFlow.value.find { it.position.x == x && it.position.y == y }
    }

    override fun legalMovesFor(x: Int, y: Int): List<PiecePosition>? {
        val piece = pieceAt(x = x, y = y) ?: return null
        return legalMoves(piece)
    }

    private fun sendUpdate(pieces: Set<Piece>) {
        _piecesFlow.update { pieces.toMutableSet() }
    }

    private fun updateCheckedKings() {
        _piecesFlow.value
            .filterIsInstance<King>()
            .map { king ->
                val isChecked = opponentsMoves(king).contains(king.position)
                king.updateCheck(isChecked = isChecked)
            }
    }

    // region Castling
    private fun canCastling(king: King, rook: Rook): Boolean {
        if (king.moved || rook.moved || king.isChecked) return false
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
    // endregion

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
        return _piecesFlow.value
            .filterIsInstance<Rook>()
            .filter { it.color == king.color }
            .mapNotNull { rook ->
                if (canCastling(king, rook)) rook.position else null
            }
    }

    private fun searchMove(
        piece: Piece,
        xDirection: Int,
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

    private fun opponentsMoves(piece: Piece): List<PiecePosition> {
        return _piecesFlow.value.filter { it.color != piece.color }
            .map(::legalMoves)
            .flatten()
            .distinct()
    }
    // endregion
}