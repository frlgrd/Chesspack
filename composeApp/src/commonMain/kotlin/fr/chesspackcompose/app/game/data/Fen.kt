package fr.chesspackcompose.app.game.data

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
            "rnbqkbnr/pppppppp/8/8/3Q1B2/1BN3N1/PPPPPPPP/R3K2R b Kkq - 0 1"
        private const val ROWS_SEPARATOR = '/'
        private const val ROOK = 'r'
        private const val KNIGHT = 'n'
        private const val BISHOP = 'b'
        private const val QUEEN = 'q'
        private const val KING = 'k'
        private const val PAWN = 'p'
    }

    fun toPieces(): MutableSet<Piece> {
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
                        if (piece is Rook) setCastling(piece)
                        pieces.add(piece)
                    }
                    x++
                }
            }
        }
        return pieces
    }

    private fun setCastling(rook: Rook) {
        val config = fen.split(ROWS_SEPARATOR)[7].substringAfter(' ')
        when {
            rook.position.x == 0 && rook.color == PieceColor.White && !config.contains('Q') -> rook.markAsMoved()
            rook.position.x == 0 && rook.color == PieceColor.Black && !config.contains('q') -> rook.markAsMoved()
            rook.position.x == 7 && rook.color == PieceColor.White && !config.contains('K') -> rook.markAsMoved()
            rook.position.x == 7 && rook.color == PieceColor.Black && !config.contains('k') -> rook.markAsMoved()
        }
    }
}