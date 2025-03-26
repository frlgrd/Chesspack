package fr.chesspackcompose.app.game.data

import fr.chesspackcompose.app.game.domain.Board
import fr.chesspackcompose.app.game.domain.MoveResult
import fr.chesspackcompose.app.game.domain.PieceColor
import fr.chesspackcompose.app.game.domain.PiecePosition
import fr.chesspackcompose.app.game.domain.Promotion
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
    private val fen: Fen
) : Board {

    private val _state = MutableStateFlow(Board.State())
    override val state: Flow<Board.State> = _state.asStateFlow()

    init {
        reset()
    }

    override fun move(from: PiecePosition, to: PiecePosition) {
        var state = _state.value
        val pieces = state.pieces
        val piece = pieces.find { it.position == from } ?: return
        val target = pieces.find { it.position == to }
        state = if (target?.color == piece.color) {
            state.castling(king = piece, rook = target)
        } else {
            state.movePiece(piece = piece, to = to, target = target)
        }
        piece.markAsMoved()
        state = state.globalUpdate()
        if (piece is Pawn && canBePromoted(piece)) {
            state = state.copy(promotion = Promotion(pawn = piece), moveResult = null)
            _state.update { state }
            return
        }
        _state.update {
            state.copy(
                pieces = pieces,
                currentPlayer = it.currentPlayer.switch(),
                playerSwitched = true
            )
        }
    }

    private fun Board.State.movePiece(
        piece: Piece,
        to: PiecePosition,
        target: Piece?
    ): Board.State {
        pieces.removeAll { it.position == to }
        piece.position = to
        return if (target != null) {
            val takenPieces = takenPieces.toMutableMap()
            if (takenPieces.containsKey(target.color)) {
                takenPieces[target.color]!!.add(target)
            } else {
                takenPieces[target.color] = mutableListOf(target)
            }
            copy(takenPieces = takenPieces, moveResult = MoveResult.Capture)
        } else {
            copy(moveResult = MoveResult.SimpleMove)
        }
    }

    override fun pieceAt(pieces: Set<Piece>, x: Int, y: Int): Piece? {
        return pieces.find { it.position.x == x && it.position.y == y }
    }

    override fun legalMoves(position: PiecePosition): List<PiecePosition> {
        return _state.value.pieces.find { it.position == position }?.legalMoves.orEmpty()
    }

    override fun promote(
        position: PiecePosition,
        color: PieceColor,
        type: Promotion.Type
    ) {
        var state = _state.value
        state.pieces.removeAll { it.position == position }
        val promotedPiece = when (type) {
            Promotion.Type.QUEEN -> Queen(position = position, color = color)
            Promotion.Type.ROOK -> Rook(position = position, color = color)
            Promotion.Type.BISHOP -> Bishop(position = position, color = color)
            Promotion.Type.KNIGHT -> Knight(position = position, color = color)
        }
        state.pieces.add(promotedPiece)
        state = state.globalUpdate()
        _state.update {
            state.copy(
                currentPlayer = it.currentPlayer.switch(),
                moveResult = state.moveResult ?: MoveResult.SimpleMove,
                promotion = null,
                playerSwitched = true
            )
        }
    }

    override fun reset() {
        _state.value = Board.State(pieces = fen.toPieces().toMutableSet())
        _state.update {
            it.copy(pieces = it.pieces.map { piece ->
                piece.updateLegalMoves(legalMoves(piece))
            }.toMutableSet())
        }
    }

    private fun Board.State.globalUpdate(): Board.State {
        return updateCheckedKings().updateLegalMoves().updateWinner()
    }

    private fun Board.State.updateCheckedKings(): Board.State {
        var hasCheckedKing = false
        val pieces = pieces.map { piece ->
            if (piece is King) {
                val opponentsMoves = opponentsMoves(
                    pieces = pieces,
                    pieceColor = piece.color
                )
                val isChecked = opponentsMoves.contains(piece.position)
                if (isChecked) {
                    hasCheckedKing = true
                }
                piece.updateCheck(isChecked = isChecked)
            }
            piece
        }
        return copy(
            pieces = pieces.toMutableSet(),
            moveResult = if (hasCheckedKing) MoveResult.Check else moveResult
        )
    }

    private fun Board.State.updateLegalMoves(): Board.State {
        return copy(pieces = pieces
            .map { it.updateLegalMoves(moves = legalMoves(it)) }
            .toMutableSet()
        )
    }

    private fun Board.State.updateWinner(): Board.State {
        val currentPlayerWon = pieces
            .filter { it.color == currentPlayer.switch() }
            .map(Piece::legalMoves)
            .flatten()
            .isEmpty()
        return if (currentPlayerWon) {
            copy(
                winner = currentPlayer,
                moveResult = MoveResult.Checkmate,
                playerSwitched = if (currentPlayerWon) false else playerSwitched
            )
        } else {
            this
        }
    }

    private fun legalMoves(piece: Piece): List<PiecePosition> {
        return pseudoLegalMoves(
            pieces = _state.value.pieces,
            piece = piece
        ).filterNot { isIllegalMove(piece = piece, position = it) }
    }

    private fun isIllegalMove(
        piece: Piece,
        position: PiecePosition,
    ): Boolean {
        val piecesAfterMove = _state.value.pieces.map(Piece::copyPiece).toMutableSet()
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

    private fun canBePromoted(pawn: Pawn): Boolean {
        return when (pawn.color) {
            PieceColor.Black -> pawn.position.y == 7
            PieceColor.White -> pawn.position.y == 0
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
            range.mapNotNull { pieceAt(pieces = _state.value.pieces, x = it, y = king.position.y) }
        return piecesBetween.isEmpty()
    }

    private fun Board.State.castling(
        king: Piece,
        rook: Piece
    ): Board.State {
        if (king.position.x < rook.position.x) {
            king.position = king.position.copy(x = king.position.x + 2)
            rook.position = rook.position.copy(x = king.position.x - 1)
        } else {
            king.position = king.position.copy(x = king.position.x - 2)
            rook.position = rook.position.copy(x = king.position.x + 1)
        }
        return copy(moveResult = MoveResult.Castling)
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
        if (x < 0 || y < 0 || x > 7 || y > 7) return null
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
        return pieces.asSequence()
            .filter { it.color != pieceColor }
            .filter { it.position != attackedPosition }
            .map { pseudoLegalMoves(pieces = pieces, piece = it) }
            .flatten()
            .distinct()
            .toList()
    }
    // endregion
}