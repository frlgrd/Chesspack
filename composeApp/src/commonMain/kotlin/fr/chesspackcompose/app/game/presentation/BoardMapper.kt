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
import fr.chesspackcompose.app.game.domain.pgnPositionX
import fr.chesspackcompose.app.game.domain.pgnPositionY
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
        boardState: Board.State
    ): List<CellUIModel> {
        val result = mutableListOf<CellUIModel>()
        (0..7).forEach { x ->
            (0..7).forEach { y ->
                val position = PiecePosition(x = x, y = y)
                val piece = boardState.pieces.find { it.position == position }
                val cellUIModel = CellUIModel(
                    position = position,
                    squareColor = squareColor(
                        boardState = boardState,
                        piece = piece,
                        position = position
                    ),
                    moveEnabled = boardState.winner == null && boardState.currentPlayer == piece?.color,
                    pieceInfo = piece.toPieceUiInfo(),
                    isChecked = boardState.winner == null && piece is King && piece.isChecked,
                    coordinateUI = position.toCoordinatesUI(currentPlayer = boardState.currentPlayer)
                )
                result.add(cellUIModel)
            }
        }
        return result
    }

    fun mapBanner(
        color: PieceColor,
        allPieces: Set<Piece>,
        takenPieces: Map<PieceColor, List<Piece>>
    ): GameBanner {
        val advantage = buildAdvantageLabel(color = color, allPieces = allPieces)
        val uiColor = if (color == PieceColor.White) Color.White else Color.Black
        val gameBanner = GameBanner(
            takenPieces = emptyMap(),
            pieceColor = color,
            textColor = uiColor,
            advantageLabel = advantage
        )
        val pieces = takenPieces[color.switch()] ?: return gameBanner
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
        return gameBanner.copy(takenPieces = piecesMap)
    }

    fun mapPromotion(promotion: Promotion?): PromotionUiModel? {
        promotion ?: return null
        return PromotionUiModel(items = promotion.pawn.promotionItem)
    }

    private fun squareColor(
        boardState: Board.State,
        piece: Piece?,
        position: PiecePosition
    ): Color {
        return when {
            boardState.winner != null && piece is King -> if (piece.color == boardState.winner) winnerColor else looserColor
            else -> if ((position.x + position.y) % 2 == 0) darkColor else lightColor
        }
    }

    private fun buildAdvantageLabel(
        color: PieceColor,
        allPieces: Set<Piece>,
    ): String {
        val colorScore = allPieces.filter { it.color == color }.sumOf { it.power }
        val otherColorScore = allPieces.filter { it.color == color.switch() }.sumOf { it.power }
        val label = if (colorScore > otherColorScore) "+ ${colorScore - otherColorScore}" else ""
        return label
    }

    private fun Piece?.toPieceUiInfo(): PieceInfo? {
        this ?: return null
        return PieceInfo(
            drawableResource = drawableResource,
            legalMoves = legalMoves
        )
    }

    private fun PiecePosition.toCoordinatesUI(
        currentPlayer: PieceColor
    ): CoordinateUI? {
        val xLimit = when (currentPlayer) {
            PieceColor.Black -> 7
            PieceColor.White -> 0
        }
        val yLimit = 7 - xLimit
        val xCoordinate = if (xLimit == x) pgnPositionX else null
        val yCoordinate = if (yLimit == y) pgnPositionY else null

        if (xCoordinate == null && yCoordinate == null) return null
        val color = if ((x + y) % 2 == 0) lightColor else darkColor
        return CoordinateUI(color = color, x = xCoordinate, y = yCoordinate)
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
                    drawableResource = Res.drawable.piece_queen_side_black,
                    type = Promotion.Type.QUEEN,
                    color = color,
                    position = position
                ),
                PromotionItem(
                    drawableResource = Res.drawable.piece_rook_side_black,
                    type = Promotion.Type.ROOK,
                    color = color,
                    position = position
                ),
                PromotionItem(
                    drawableResource = Res.drawable.piece_bishop_side_black,
                    type = Promotion.Type.BISHOP,
                    color = color,
                    position = position
                ),
                PromotionItem(
                    drawableResource = Res.drawable.piece_knight_side_black,
                    type = Promotion.Type.KNIGHT,
                    color = color,
                    position = position
                )
            )

            PieceColor.White -> listOf(
                PromotionItem(
                    drawableResource = Res.drawable.piece_queen_side_white,
                    type = Promotion.Type.QUEEN,
                    color = color,
                    position = position
                ),
                PromotionItem(
                    drawableResource = Res.drawable.piece_rook_side_white,
                    type = Promotion.Type.ROOK,
                    color = color,
                    position = position
                ),
                PromotionItem(
                    drawableResource = Res.drawable.piece_bishop_side_white,
                    type = Promotion.Type.BISHOP,
                    color = color,
                    position = position
                ),
                PromotionItem(
                    drawableResource = Res.drawable.piece_knight_side_white,
                    type = Promotion.Type.KNIGHT,
                    color = color,
                    position = position
                )
            )
        }
}