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
import kotlin.reflect.KClass

/**
 * FEN (Forsyth-Edwards Notation) represents the board state. See more @see [here](https://fr.wikipedia.org/wiki/Notation_Forsyth-Edwards)
 */

@JvmInline
value class Fen(
    private val fen: String = DEFAULT
) {
    companion object {
        private const val DEFAULT = "rnbqkbnr/2p1pN2/8/3P4/3p4/8/2P1PP2/R1Q1KBNR w KQkq - 0 1"
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
            playerSwitched = currentPlayer == PieceColor.Black,
            takenPieces = resolveTakenPieces(pieces = pieces)
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
                        if (piece is Rook) markRook(piece)
                        if (piece is Pawn) markPawn(piece)
                        pieces.add(piece)
                    }
                    x++
                }
            }
        }
        return pieces
    }

    private fun markRook(rook: Rook) {
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

    private fun markPawn(pawn: Pawn) {
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

    private fun resolveTakenPieces(
        pieces: Set<Piece>
    ): Map<PieceColor, MutableList<Piece>> {

        fun getTakenPieces(pieces: Set<Piece>, color: PieceColor): MutableList<Piece> {
            fun missingPieces(
                pieces: List<Piece>,
                type: KClass<out Piece>,
                expectedCount: Int,
                createMissingPiece: () -> Piece
            ): MutableList<Piece> {
                val actualCount = pieces.count(type::isInstance)
                val missing = mutableListOf<Piece>()
                repeat(expectedCount - actualCount) {
                    missing.add(createMissingPiece())
                }
                return missing
            }

            val enemies = pieces.filter { it.color == color }
            val takenPiecesPosition = PiecePosition(0, 0)
            val takenPieces = missingPieces(
                pieces = enemies,
                type = Queen::class,
                expectedCount = 1,
                createMissingPiece = { Queen(takenPiecesPosition, color = color) }) +
                    missingPieces(
                        pieces = enemies,
                        type = Rook::class,
                        expectedCount = 2,
                        createMissingPiece = {
                            Rook(takenPiecesPosition, color = color)
                        }) +
                    missingPieces(
                        pieces = enemies,
                        type = Knight::class,
                        expectedCount = 2,
                        createMissingPiece = {
                            Knight(takenPiecesPosition, color = color)
                        }) +
                    missingPieces(
                        pieces = enemies,
                        type = Bishop::class,
                        expectedCount = 2,
                        createMissingPiece = {
                            Bishop(takenPiecesPosition, color = color)
                        }) +
                    missingPieces(
                        pieces = enemies,
                        type = Pawn::class,
                        expectedCount = 8,
                        createMissingPiece = {
                            Pawn(takenPiecesPosition, color = color)
                        })
            return takenPieces.toMutableList()
        }

        val takenPieces = mutableMapOf<PieceColor, MutableList<Piece>>()
        takenPieces[PieceColor.White] = getTakenPieces(pieces = pieces, color = PieceColor.White)
        takenPieces[PieceColor.Black] = getTakenPieces(pieces = pieces, color = PieceColor.Black)
        return takenPieces
    }
}