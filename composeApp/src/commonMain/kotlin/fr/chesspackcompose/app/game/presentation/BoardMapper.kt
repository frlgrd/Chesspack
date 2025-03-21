package fr.chesspackcompose.app.game.presentation

import androidx.compose.ui.graphics.Color
import chesspackcompose.composeapp.generated.resources.Res
import chesspackcompose.composeapp.generated.resources.piece_bishop_side_black
import chesspackcompose.composeapp.generated.resources.piece_bishop_side_white
import chesspackcompose.composeapp.generated.resources.piece_king_side_black
import chesspackcompose.composeapp.generated.resources.piece_king_side_white
import chesspackcompose.composeapp.generated.resources.piece_knight_side_black
import chesspackcompose.composeapp.generated.resources.piece_knight_side_white
import chesspackcompose.composeapp.generated.resources.piece_pawn_side_black
import chesspackcompose.composeapp.generated.resources.piece_pawn_side_white
import chesspackcompose.composeapp.generated.resources.piece_queen_side_black
import chesspackcompose.composeapp.generated.resources.piece_queen_side_white
import chesspackcompose.composeapp.generated.resources.piece_rook_side_black
import chesspackcompose.composeapp.generated.resources.piece_rook_side_white
import fr.chesspackcompose.app.game.domain.Board
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
import org.jetbrains.compose.resources.DrawableResource

class BoardMapper {
    fun mapPieces(
        board: Board,
        pieces: Set<Piece>,
        player: PieceColor
    ): List<CellUIModel> {
        val result = mutableListOf<CellUIModel>()
        (0..7).forEach { x ->
            (0..7).forEach { y ->
                val position = PiecePosition(x = x, y = y)
                val piece = pieces.find { it.position == position }
                val cellUIModel = CellUIModel(
                    position = position,
                    squareColor = squareColor(board = board, piece = piece, position = position),
                    moveEnabled = board.winner == null && player == piece?.color,
                    pieceInfo = piece.toPieceUiInfo(board),
                    isChecked = board.winner == null && piece is King && piece.isChecked
                )
                result.add(cellUIModel)
            }
        }
        return result
    }

    private fun squareColor(board: Board, piece: Piece?, position: PiecePosition): Color {
        return when {
            board.winner != null && piece is King -> if (piece.color == board.winner) winnerColor else looserColor
            else -> if ((position.x + position.y) % 2 == 0) darkColor else lightColor
        }
    }

    fun mapTakenPieces(color: PieceColor, takenPieces: Map<PieceColor, List<Piece>>): TakenPieces? {
        val pieces = takenPieces[color] ?: return null
        if (pieces.isEmpty()) return null
        val piecesMap = mutableMapOf<DrawableResource, TakenPiece>()
        pieces.groupBy { it::class }.map {
            val piece = it.value.first()
            piecesMap.put(
                key = piece.drawableResource,
                value = TakenPiece(
                    order = piece.takenPieceOrder,
                    count = it.value.size
                )
            )
        }
        return TakenPieces(
            pieces = piecesMap,
            advantageLabel = buildAdvantageLabel(
                color = color,
                takenPieces = takenPieces
            )
        )
    }

    fun mapPromotion(promotion: Promotion?): PromotionUiModel? {
        promotion ?: return null
        return PromotionUiModel(items = promotion.pawn.promotionItem)
    }

    private fun buildAdvantageLabel(
        color: PieceColor,
        takenPieces: Map<PieceColor, List<Piece>>
    ): String? {
        val otherColor = color.switch()
        val colorScore = takenPieces[color].orEmpty().sumOf { it.power }
        val otherColorScore = takenPieces[otherColor].orEmpty().sumOf { it.power }
        return if (colorScore > otherColorScore) {
            "+ ${colorScore - otherColorScore}"
        } else {
            null
        }
    }

    private fun Piece?.toPieceUiInfo(board: Board): PieceInfo? {
        this ?: return null
        return PieceInfo(
            drawableResource = drawableResource,
            legalMoves = board.legalMoves(position)
        )
    }

    private val Piece.takenPieceOrder: Int
        get() = when (this) {
            is Bishop -> 2
            is King -> 5
            is Knight -> 1
            is Pawn -> 0
            is Queen -> 4
            is Rook -> 3
        }

    private val Piece.drawableResource: DrawableResource
        get() = when (color) {
            PieceColor.Black -> when (this) {
                is Bishop -> Res.drawable.piece_bishop_side_black
                is King -> Res.drawable.piece_king_side_black
                is Knight -> Res.drawable.piece_knight_side_black
                is Pawn -> Res.drawable.piece_pawn_side_black
                is Queen -> Res.drawable.piece_queen_side_black
                is Rook -> Res.drawable.piece_rook_side_black
            }

            PieceColor.White -> when (this) {
                is Bishop -> Res.drawable.piece_bishop_side_white
                is King -> Res.drawable.piece_king_side_white
                is Knight -> Res.drawable.piece_knight_side_white
                is Pawn -> Res.drawable.piece_pawn_side_white
                is Queen -> Res.drawable.piece_queen_side_white
                is Rook -> Res.drawable.piece_rook_side_white
            }
        }

    private val Piece.promotionItem: List<PromotionItem>
        get() = when (color) {
            PieceColor.Black -> listOf(
                PromotionItem(
                    Res.drawable.piece_queen_side_black,
                    Promotion.Type.QUEEN,
                    color,
                    position
                ),
                PromotionItem(
                    Res.drawable.piece_rook_side_black,
                    Promotion.Type.ROOK,
                    color,
                    position
                ),
                PromotionItem(
                    Res.drawable.piece_bishop_side_black,
                    Promotion.Type.BISHOP,
                    color,
                    position
                ),
                PromotionItem(
                    Res.drawable.piece_knight_side_black,
                    Promotion.Type.KNIGHT,
                    color,
                    position
                )
            )

            PieceColor.White -> listOf(
                PromotionItem(
                    Res.drawable.piece_queen_side_white,
                    Promotion.Type.QUEEN,
                    color,
                    position
                ),
                PromotionItem(
                    Res.drawable.piece_rook_side_white,
                    Promotion.Type.ROOK,
                    color,
                    position
                ),
                PromotionItem(
                    Res.drawable.piece_bishop_side_white,
                    Promotion.Type.BISHOP,
                    color,
                    position
                ),
                PromotionItem(
                    Res.drawable.piece_knight_side_white,
                    Promotion.Type.KNIGHT,
                    color,
                    position
                )
            )
        }
}