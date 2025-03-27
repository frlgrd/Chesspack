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
import kotlin.math.abs

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
        state = state.update()
        state = when {
            piece is Pawn && canBePromoted(piece) -> state.copy(
                promotion = Promotion(pawn = piece),
                moveResult = null,
                playerSwitched = false
            )

            else -> state.copy(
                pieces = pieces,
                currentPlayer = state.currentPlayer.switch(),
                playerSwitched = state.winner == null
            )
        }
        _state.update { state }
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
        state = state.update()
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
        _state.value = fen.toBoardState().update()
    }

    private fun Board.State.movePiece(
        piece: Piece,
        to: PiecePosition,
        target: Piece?
    ): Board.State {
        pieces.removeAll { it.position == to }
        val originalPosition = piece.position
        piece.position = to
        return when {
            target != null -> {
                pieceTaken(target = target)
            }

            isEnPassant(
                piece = piece,
                enPassant = enPassant
            ) -> {
                pieces.removeAll { it.position == enPassant?.position }
                pieceTaken(target = enPassant)
            }

            else -> copy(
                moveResult = MoveResult.SimpleMove,
                enPassant = enPassant(piece = piece, originalPosition = originalPosition)
            )
        }
    }

    private fun Board.State.pieceTaken(target: Piece?): Board.State {
        target ?: return this
        val takenPieces = takenPieces.toMutableMap()
        if (takenPieces.containsKey(target.color)) {
            takenPieces[target.color]!!.add(target)
        } else {
            takenPieces[target.color] = mutableListOf(target)
        }
        return copy(takenPieces = takenPieces, moveResult = MoveResult.Capture)
    }

    private fun isEnPassant(piece: Piece, enPassant: Pawn?): Boolean {
        enPassant ?: return false
        if (piece !is Pawn) return false
        if (piece.color == enPassant.color) return false
        if (piece.position.x != enPassant.position.x) return false
        val direction = if (piece.color == PieceColor.White) -1 else 1
        return piece.position.y == enPassant.position.y + direction
    }

    private fun enPassant(piece: Piece, originalPosition: PiecePosition): Pawn? {
        if (piece !is Pawn) return null
        val distance = abs(originalPosition.y - piece.position.y)
        return if (distance == 2) piece else null
    }

    private fun Board.State.update(): Board.State {
        return updateCheckedKings()
            .updateLegalMoves()
            .updateWinner()
    }

    private fun Board.State.updateCheckedKings(): Board.State {
        var hasCheckedKing = false
        val pieces = pieces.map { piece ->
            if (piece is King) {
                val opponentsMoves = opponentsMoves(
                    pieces = pieces,
                    pieceColor = piece.color,
                    enPassant = enPassant
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
        return copy(pieces = pieces.map { piece ->
            val pseudoLegalMoves =
                pseudoLegalMoves(pieces = pieces, piece = piece, enPassant = enPassant)
            piece.updatePseudoLegalMoves(moves = pseudoLegalMoves)
            val legalMoves = pseudoLegalMoves.filterNot { position ->
                isIllegalMove(
                    pieces = pieces,
                    piece = piece,
                    position = position,
                    enPassant = enPassant
                )
            }
            piece.updateLegalMoves(moves = legalMoves)
        }.toMutableSet())
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
                moveResult = MoveResult.Checkmate
            )
        } else {
            this
        }
    }

    private fun isIllegalMove(
        pieces: Set<Piece>,
        piece: Piece,
        position: PiecePosition,
        enPassant: Pawn?
    ): Boolean {
        val piecesAfterMove = pieces.map(Piece::copyPiece).toMutableSet()
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
            attackedPosition = position,
            enPassant = enPassant
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
        pieces: Set<Piece>,
        king: King,
        rook: Rook
    ): Boolean {
        if (king.moved || rook.moved) return false

        val kingPath = if (king.position.x < rook.position.x) {
            king.position.x..<king.position.x + 2
        } else {
            king.position.x downTo king.position.x - 2
        }.map { PiecePosition(x = it, y = king.position.y) }
        val opponentsMoves = pieces.filter { it.color != king.color }
            .map { it.pseudoLegalMoves }
            .flatten()
            .distinct()
        kingPath.forEach { kingPosition ->
            if (opponentsMoves.contains(kingPosition)) return false
        }

        val range = if (king.position.x < rook.position.x) {
            king.position.x + 1..<rook.position.x // pieces between king and east rook
        } else {
            king.position.x - 1 downTo rook.position.x + 1 // pieces between king and west rook
        }
        val piecesBetween = range.mapNotNull {
            pieceAt(pieces = pieces, x = it, y = king.position.y)
        }
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
        piece: Piece,
        enPassant: Pawn?,
    ): List<PiecePosition> {
        val pseudoLegalMoves = mutableListOf<PiecePosition>()
        when (piece) {
            is Bishop -> diagonalMoves(pieces = pieces, piece = piece, moves = pseudoLegalMoves)
            is King -> kingMoves(pieces = pieces, king = piece, moves = pseudoLegalMoves)
            is Knight -> knightMoves(pieces = pieces, piece = piece, moves = pseudoLegalMoves)
            is Pawn -> pawnMoves(
                pieces = pieces,
                pawn = piece,
                moves = pseudoLegalMoves,
                enPassant = enPassant
            )

            is Queen -> queenMoves(pieces = pieces, piece = piece, moves = pseudoLegalMoves)
            is Rook -> straightMoves(pieces = pieces, piece = piece, moves = pseudoLegalMoves)
        }
        return pseudoLegalMoves.distinct()
    }

    private fun queenMoves(
        pieces: Set<Piece>,
        piece: Piece,
        moves: MutableList<PiecePosition>
    ) {
        straightMoves(pieces = pieces, piece = piece, moves = moves)
        diagonalMoves(pieces = pieces, piece = piece, moves = moves)
    }

    private fun kingMoves(
        pieces: Set<Piece>,
        king: King,
        moves: MutableList<PiecePosition>
    ) {
        diagonalMoves(pieces = pieces, piece = king, max = 1, moves = moves)
        straightMoves(pieces = pieces, piece = king, max = 1, moves = moves)
        castlingMoves(pieces = pieces, king = king, moves = moves)
    }

    private fun knightMoves(
        pieces: Set<Piece>,
        piece: Piece,
        moves: MutableList<PiecePosition>,
    ) {
        moves.addAll(
            listOfNotNull(
                jumpOverMove(pieces = pieces, piece = piece, xDirection = -1, yDirection = -2),
                jumpOverMove(pieces = pieces, piece = piece, xDirection = 1, yDirection = -2),
                jumpOverMove(pieces = pieces, piece = piece, xDirection = -1, yDirection = 2),
                jumpOverMove(pieces = pieces, piece = piece, xDirection = 1, yDirection = 2),
                jumpOverMove(pieces = pieces, piece = piece, xDirection = -2, yDirection = -1),
                jumpOverMove(pieces = pieces, piece = piece, xDirection = 2, yDirection = -1),
                jumpOverMove(pieces = pieces, piece = piece, xDirection = -2, yDirection = 1),
                jumpOverMove(pieces = pieces, piece = piece, xDirection = 2, yDirection = 1),
            )
        )
    }

    private fun straightMoves(
        pieces: Set<Piece>,
        piece: Piece,
        moves: MutableList<PiecePosition>,
        max: Int = 7
    ) {
        searchMove(
            pieces = pieces,
            piece = piece,
            xDirection = -1,
            yDirection = 0,
            moves = moves,
            max = max
        )
        searchMove(
            pieces = pieces,
            piece = piece,
            xDirection = 0,
            yDirection = -1,
            moves = moves,
            max = max
        )
        searchMove(
            pieces = pieces,
            piece = piece,
            xDirection = 1,
            yDirection = 0,
            moves = moves,
            max = max
        )
        searchMove(
            pieces = pieces,
            piece = piece,
            xDirection = 0,
            yDirection = 1,
            moves = moves,
            max = max
        )
    }

    private fun diagonalMoves(
        pieces: Set<Piece>,
        piece: Piece,
        moves: MutableList<PiecePosition>,
        max: Int = 7
    ) {
        searchMove(
            pieces = pieces,
            piece = piece,
            xDirection = 1,
            yDirection = 1,
            moves = moves,
            max = max
        )
        searchMove(
            pieces = pieces,
            piece = piece,
            xDirection = -1,
            yDirection = 1,
            moves = moves,
            max = max
        )
        searchMove(
            pieces = pieces,
            piece = piece,
            xDirection = -1,
            yDirection = -1,
            moves = moves,
            max = max
        )
        searchMove(
            pieces = pieces,
            piece = piece,
            xDirection = 1,
            yDirection = -1,
            moves = moves,
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
        pawn: Pawn,
        moves: MutableList<PiecePosition>,
        enPassant: Pawn?
    ) {
        val direction = if (pawn.color == PieceColor.White) -1 else 1
        searchMove(
            pieces = pieces,
            piece = pawn,
            xDirection = 0,
            yDirection = direction,
            moves = moves,
            max = if (pawn.moved) 1 else 2,
            canAttackFromFront = false
        )
        pawnAttackMoves(
            pieces = pieces,
            piece = pawn,
            direction = direction,
            moves = moves
        )
        pawnEnPassantAttack(
            piece = pawn,
            enPassant = enPassant,
            direction = direction,
            moves = moves
        )
    }

    private fun pawnAttackMoves(
        pieces: Set<Piece>,
        piece: Piece,
        direction: Int,
        moves: MutableList<PiecePosition>,
    ) {
        val leftPiece =
            pieceAt(pieces = pieces, x = piece.position.x - 1, y = piece.position.y + direction)
        if (leftPiece != null && leftPiece.color != piece.color) {
            moves.add(leftPiece.position)
        }
        val rightPiece =
            pieceAt(pieces = pieces, x = piece.position.x + 1, y = piece.position.y + direction)
        if (rightPiece != null && rightPiece.color != piece.color) {
            moves.add(rightPiece.position)
        }
    }

    private fun pawnEnPassantAttack(
        piece: Piece,
        enPassant: Pawn?,
        direction: Int,
        moves: MutableList<PiecePosition>
    ) {
        enPassant ?: return
        if (enPassant == piece) return
        if (enPassant.color == piece.color) return
        if (enPassant.position.y != piece.position.y) return
        val distance = abs(enPassant.position.x - piece.position.x)
        if (distance != 1) return
        moves.add(PiecePosition(x = enPassant.position.x, y = piece.position.y + direction))
    }

    private fun castlingMoves(
        pieces: Set<Piece>,
        king: King,
        moves: MutableList<PiecePosition>
    ) {
        moves.addAll(pieces
            .filterIsInstance<Rook>()
            .filter { it.color == king.color }
            .filter { rook -> canCastling(pieces = pieces, king = king, rook = rook) }
            .map { it.position }
        )
    }

    private fun searchMove(
        pieces: Set<Piece>,
        piece: Piece,
        xDirection: Int,
        yDirection: Int,
        moves: MutableList<PiecePosition>,
        max: Int,
        canAttackFromFront: Boolean = true
    ) {
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
    }

    private fun pieceAt(pieces: Set<Piece>, x: Int, y: Int): Piece? {
        return pieces.find { it.position.x == x && it.position.y == y }
    }

    private fun opponentsMoves(
        pieces: Set<Piece>,
        pieceColor: PieceColor,
        attackedPosition: PiecePosition? = null,
        enPassant: Pawn?
    ): List<PiecePosition> {
        return pieces.asSequence()
            .filter { it.color != pieceColor }
            .filter { it.position != attackedPosition }
            .map { pseudoLegalMoves(pieces = pieces, piece = it, enPassant = enPassant) }
            .flatten()
            .distinct()
            .toList()
    }
    // endregion
}