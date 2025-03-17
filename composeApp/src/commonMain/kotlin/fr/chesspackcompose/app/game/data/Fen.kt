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
        private const val DEFAULT = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        private const val ROWS_SEPARATOR = '/'
        private const val ROOK = 'r'
        private const val KNIGHT = 'n'
        private const val BISHOP = 'b'
        private const val QUEEN = 'q'
        private const val KING = 'k'
        private const val PAWN = 'p'
    }

    fun pieceAt(x: Int, y: Int): Piece? {
        var colIndex = 0
        fen.split(ROWS_SEPARATOR)[y].forEach { char ->
            if (char.isDigit()) {
                colIndex += char.digitToInt()
            } else {
                if (colIndex == x) {
                    val color = if (char.isLowerCase()) PieceColor.Black else PieceColor.White
                    return when (char.lowercaseChar()) {
                        ROOK -> Rook(position = PiecePosition(x = x, y = y), color = color)
                        KNIGHT -> Knight(position = PiecePosition(x = x, y = y), color = color)
                        BISHOP -> Bishop(position = PiecePosition(x = x, y = y), color = color)
                        QUEEN -> Queen(position = PiecePosition(x = x, y = y), color = color)
                        KING -> King(position = PiecePosition(x = x, y = y), color = color)
                        PAWN -> Pawn(position = PiecePosition(x = x, y = y), color = color)
                        else -> null
                    }
                }
                colIndex++
            }
        }
        return null
    }
}