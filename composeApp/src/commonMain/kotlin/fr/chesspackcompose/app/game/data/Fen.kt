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
import kotlin.jvm.JvmInline

/**
 * FEN (Forsyth-Edwards Notation) represents the board state. See more @see [here](https://fr.wikipedia.org/wiki/Notation_Forsyth-Edwards)
 */

@JvmInline
value class Fen(
    private val fen: String = DEFAULT
) {
    companion object {
        private const val DEFAULT =
            "r3k2r/p1pppppp/1pbq2n1/5b2/3Q1B2/1BN1P1N1/PPPP1PPP/R3K2R w KQkq - 0 1\n"
        private const val ROWS_SEPARATOR = '/'
        private const val ROOK = 'r'
        private const val KNIGHT = 'n'
        private const val BISHOP = 'b'
        private const val QUEEN = 'q'
        private const val KING = 'k'
        private const val PAWN = 'p'
    }

    fun toBoardState(): Board.State {
        val pieces = resolvePieces()
        val currentPlayer = resolvePlayer()
        return Board.State(
            pieces = pieces,
            currentPlayer = currentPlayer,
            playerSwitched = currentPlayer == PieceColor.Black
        )
    }

    private fun resolvePieces(): MutableSet<Piece> {
        val pieces = mutableSetOf<Piece>()
        fen.split(ROWS_SEPARATOR).forEachIndexed { y, row ->
            var x = 0
            row.substringBefore(" ").forEach { char ->
                if (char.isDigit()) {
                    x += char.digitToInt()
                } else {
                    val color = if (char.isLowerCase()) PieceColor.Black else PieceColor.White
                    val position = PiecePosition(x = x, y = y)
                    val piece = when (char.lowercaseChar()) {
                        ROOK -> Rook(position = position, color = color)
                        KNIGHT -> Knight(position = position, color = color)
                        BISHOP -> Bishop(position = position, color = color)
                        QUEEN -> Queen(position = position, color = color)
                        KING -> King(position = position, color = color)
                        PAWN -> Pawn(position = position, color = color)
                        else -> null
                    }
                    if (piece != null) {
                        if (piece is Rook) initRook(piece)
                        if (piece is Pawn) initPawn(piece)
                        pieces.add(piece)
                    }
                    x++
                }
            }
        }
        return pieces
    }

    private fun initRook(rook: Rook) {
        val lastRow = fen.split(ROWS_SEPARATOR)[7]
        if (!lastRow.contains(' ')) {
            rook.markAsMoved()
            return
        }
        val config = lastRow.substringAfter(' ')
        val isWhiteQueenRook = rook.position.x == 0 && rook.color == PieceColor.White
        val isWhiteKinRook = rook.position.x == 7 && rook.color == PieceColor.White
        val isBlackQueenRook = rook.position.x == 0 && rook.color == PieceColor.Black
        val isBlackKinRook = rook.position.x == 7 && rook.color == PieceColor.Black
        when {
            isWhiteQueenRook && !config.contains('Q') -> rook.markAsMoved()
            isWhiteKinRook && !config.contains('K') -> rook.markAsMoved()
            isBlackQueenRook && !config.contains('q') -> rook.markAsMoved()
            isBlackKinRook && !config.contains('k') -> rook.markAsMoved()
        }
    }

    private fun initPawn(pawn: Pawn) {
        if (pawn.color == PieceColor.Black && pawn.position.y != 1) pawn.markAsMoved()
        if (pawn.color == PieceColor.White && pawn.position.y != 6) pawn.markAsMoved()
    }

    private fun resolvePlayer(): PieceColor {
        val lastRow = fen.split(ROWS_SEPARATOR)[7]
        if (!lastRow.contains(' ')) {
            return PieceColor.White
        }
        return if (lastRow.contains("w")) {
            PieceColor.White
        } else {
            PieceColor.Black
        }
    }
}