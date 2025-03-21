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
    private val _takenPieces: MutableStateFlow<Map<PieceColor, MutableList<Piece>>> =
        MutableStateFlow(mutableMapOf())
    override val piecesFLow: Flow<Set<Piece>> get() = _piecesFlow.asStateFlow()
    override val player: Flow<PieceColor> get() = _player.asStateFlow()
    override val takenPieces: Flow<Map<PieceColor, List<Piece>>> get() = _takenPieces

    init {
        _piecesFlow.value = fen.toPieces().toMutableSet()
        updateCheckedKings()
        sendUpdate(pieces = _piecesFlow.value)
    }

    override fun move(from: PiecePosition, to: PiecePosition) {
        val pieces = _piecesFlow.value
        val piece = pieces.find { it.position == from } ?: return
        val target = pieces.find { it.position == to }
        if (target?.color == piece.color) {
            castling(king = piece, rook = target)
        } else {
            movePiece(piece = piece, to = to, target = target)
        }
        piece.markAsMoved()
        updateCheckedKings()
        switchPlayer()
        sendUpdate(pieces = pieces)
    }

    private fun switchPlayer() {
        _player.value = _player.value.switch()
    }

    private fun movePiece(piece: Piece, to: PiecePosition, target: Piece?) {
        _piecesFlow.value.removeAll { it.position == to }
        piece.position = to
        if (target != null) {
            val taken = _takenPieces.value.toMutableMap()
            if (taken.containsKey(target.color)) {
                taken[target.color]!!.add(target)
            } else {
                taken[target.color] = mutableListOf(target)
            }
            _takenPieces.update { taken }
        }
    }

    override fun pieceAt(pieces: Set<Piece>, x: Int, y: Int): Piece? {
        return pieces.find { it.position.x == x && it.position.y == y }
    }

    override fun legalMoves(piecePosition: PiecePosition): List<PiecePosition>? {
        val piece = pieceAt(
            pieces = _piecesFlow.value,
            x = piecePosition.x,
            y = piecePosition.y
        ) ?: return null

        return pseudoLegalMoves(
            pieces = _piecesFlow.value,
            piece = piece
        ).filterNot { isIllegalMove(piece = piece, position = it) }
    }

    private fun isIllegalMove(
        piece: Piece,
        position: PiecePosition,
    ): Boolean {
        val piecesAfterMove = _piecesFlow.value.map(Piece::copyPiece).toMutableSet()
        piecesAfterMove.removeAll { it.position == position }
        val updatedPiece = piecesAfterMove.find {
            it.position == piece.position
        }
        updatedPiece?.position = position
        val kingPosition = piecesAfterMove
            .filterIsInstance<King>()
            .first { it.color == piece.color }
            .position

        val opponentsMoves = opponentsMoves(
            pieces = piecesAfterMove,
            pieceColor = piece.color,
            attackedPosition = position
        )
        return opponentsMoves.contains(kingPosition)
    }

    private fun sendUpdate(pieces: Set<Piece>) {
        _piecesFlow.update { pieces.toMutableSet() }
    }

    private fun updateCheckedKings() {
        _piecesFlow.value
            .filterIsInstance<King>()
            .map { king ->
                val isChecked = opponentsMoves(
                    pieces = _piecesFlow.value,
                    pieceColor = king.color,
                ).contains(king.position)
                king.updateCheck(isChecked = isChecked)
            }
    }

    // region Castling
    private fun canCastling(
        king: King,
        rook: Rook
    ): Boolean {
        if (king.moved || rook.moved || king.isChecked) return false
        val range = if (king.position.x < rook.position.x) {
            king.position.x + 1..<rook.position.x // pieces between king and east rook
        } else {
            king.position.x - 1 downTo rook.position.x + 1 // pieces between king and west rook
        }
        val piecesBetween =
            range.mapNotNull { pieceAt(pieces = _piecesFlow.value, x = it, y = king.position.y) }
        return piecesBetween.isEmpty()
    }

    private fun castling(
        king: Piece,
        rook: Piece
    ) {
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
    private fun pseudoLegalMoves(
        pieces: Set<Piece>,
        piece: Piece
    ): List<PiecePosition> = when (piece) {
        is Bishop -> diagonalMoves(pieces = pieces, piece = piece)
        is King -> kingMoves(pieces = pieces, king = piece)
        is Knight -> knightMoves(pieces = pieces, piece = piece)
        is Pawn -> pawnMoves(pieces = pieces, pawn = piece)
        is Queen -> queenMoves(pieces = pieces, piece = piece)
        is Rook -> straightMoves(pieces = pieces, piece = piece)
    }

    private fun queenMoves(
        pieces: Set<Piece>,
        piece: Piece
    ): List<PiecePosition> {
        return straightMoves(pieces = pieces, piece = piece) +
                diagonalMoves(pieces = pieces, piece = piece)
    }

    private fun kingMoves(
        pieces: Set<Piece>,
        king: King
    ): List<PiecePosition> {
        return diagonalMoves(pieces = pieces, piece = king, max = 1) +
                straightMoves(pieces = pieces, piece = king, max = 1) +
                castlingMoves(pieces = pieces, king = king)
    }

    private fun knightMoves(
        pieces: Set<Piece>,
        piece: Piece
    ): List<PiecePosition> = listOfNotNull(
        jumpOverMove(pieces = pieces, piece = piece, xDirection = -1, yDirection = -2),
        jumpOverMove(pieces = pieces, piece = piece, xDirection = 1, yDirection = -2),
        jumpOverMove(pieces = pieces, piece = piece, xDirection = -1, yDirection = 2),
        jumpOverMove(pieces = pieces, piece = piece, xDirection = 1, yDirection = 2),
        jumpOverMove(pieces = pieces, piece = piece, xDirection = -2, yDirection = -1),
        jumpOverMove(pieces = pieces, piece = piece, xDirection = 2, yDirection = -1),
        jumpOverMove(pieces = pieces, piece = piece, xDirection = -2, yDirection = 1),
        jumpOverMove(pieces = pieces, piece = piece, xDirection = 2, yDirection = 1),
    )

    private fun straightMoves(
        pieces: Set<Piece>,
        piece: Piece,
        max: Int = 7
    ): List<PiecePosition> {
        return searchMove(
            pieces = pieces,
            piece = piece,
            xDirection = -1,
            yDirection = 0,
            max = max
        ) + searchMove(
            pieces = pieces,
            piece = piece,
            xDirection = 0,
            yDirection = -1,
            max = max
        ) + searchMove(
            pieces = pieces,
            piece = piece,
            xDirection = 1,
            yDirection = 0,
            max = max
        ) + searchMove(
            pieces = pieces,
            piece = piece,
            xDirection = 0,
            yDirection = 1,
            max = max
        )
    }

    private fun diagonalMoves(
        pieces: Set<Piece>,
        piece: Piece,
        max: Int = 7
    ): List<PiecePosition> {
        return searchMove(
            pieces = pieces,
            piece = piece,
            xDirection = 1,
            yDirection = 1,
            max = max
        ) + searchMove(
            pieces = pieces,
            piece = piece,
            xDirection = -1,
            yDirection = 1,
            max = max
        ) + searchMove(
            pieces = pieces,
            piece = piece,
            xDirection = -1,
            yDirection = -1,
            max = max
        ) + searchMove(
            pieces = pieces,
            piece = piece,
            xDirection = 1,
            yDirection = -1,
            max = max
        )
    }

    private fun jumpOverMove(
        pieces: Set<Piece>,
        piece: Piece,
        xDirection: Int,
        yDirection: Int
    ): PiecePosition? {
        val x = piece.position.x + xDirection
        val y = piece.position.y + yDirection
        if (x == -1 || y == -1 || x == 8 || y == 8) return null
        val pieceAt = pieceAt(pieces = pieces, x = x, y = y)
        if (pieceAt != null && pieceAt.color == piece.color) return null
        return PiecePosition(x = x, y = y)
    }

    private fun pawnMoves(
        pieces: Set<Piece>,
        pawn: Pawn
    ): List<PiecePosition> {
        val direction = if (pawn.color == PieceColor.White) -1 else 1
        return searchMove(
            pieces = pieces,
            piece = pawn,
            xDirection = 0,
            yDirection = direction,
            max = if (pawn.moved) 1 else 2,
            canAttackFromFront = false
        ) + pawnAttackMoves(pieces = pieces, piece = pawn, direction = direction)
    }

    private fun pawnAttackMoves(
        pieces: Set<Piece>,
        piece: Piece,
        direction: Int
    ): List<PiecePosition> {
        val attackMoves = mutableListOf<PiecePosition>()
        val leftPiece =
            pieceAt(pieces = pieces, x = piece.position.x - 1, y = piece.position.y + direction)
        if (leftPiece != null && leftPiece.color != piece.color) {
            attackMoves.add(leftPiece.position)
        }
        val rightPiece =
            pieceAt(pieces = pieces, x = piece.position.x + 1, y = piece.position.y + direction)
        if (rightPiece != null && rightPiece.color != piece.color) {
            attackMoves.add(rightPiece.position)
        }
        return attackMoves
    }

    private fun castlingMoves(
        pieces: Set<Piece>,
        king: King
    ): List<PiecePosition> {
        return pieces
            .filterIsInstance<Rook>()
            .filter { it.color == king.color }
            .mapNotNull { rook ->
                if (canCastling(king, rook)) rook.position else null
            }
    }

    private fun searchMove(
        pieces: Set<Piece>,
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
            val pieceAt = pieceAt(pieces = pieces, x = x, y = y)
            if (pieceAt != null && pieceAt.color == piece.color) break
            val enemyMet = pieceAt != null && pieceAt.color != piece.color
            if (enemyMet && !canAttackFromFront) break
            moves.add(PiecePosition(x = x, y = y))
            move++
        } while (!enemyMet && move < max)
        return moves
    }

    private fun opponentsMoves(
        pieces: Set<Piece>,
        pieceColor: PieceColor,
        attackedPosition: PiecePosition? = null
    ): List<PiecePosition> {
        val opponents = pieces.filter { it.color != pieceColor }
        val moves = opponents
            .filter { it.position != attackedPosition }
            .map { pseudoLegalMoves(pieces = pieces, piece = it) }
        return moves.toMutableSet()
            .flatten()
            .distinct()
    }
    // endregion
}