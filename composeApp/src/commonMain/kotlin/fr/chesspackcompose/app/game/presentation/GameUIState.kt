package fr.chesspackcompose.app.game.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import fr.chesspackcompose.app.game.domain.PieceColor
import fr.chesspackcompose.app.game.domain.PiecePosition
import fr.chesspackcompose.app.game.domain.Promotion
import fr.chesspackcompose.app.match_making.domain.Match
import fr.chesspackcompose.app.match_making.domain.getInitialBoardRotation
import fr.chesspackcompose.app.match_making.domain.getPlayerColor
import org.jetbrains.compose.resources.DrawableResource

data class GameUIState(
    val match: Match,
    val cells: List<CellUIModel> = emptyList(),
    val withesGameBanner: GameBanner? = null,
    val blacksGameBanner: GameBanner? = null,
    val promotionUiModel: PromotionUiModel? = null,
    val currentPlayer: PieceColor = PieceColor.White,
    val winner: PieceColor? = null,
    var whiteTimer: TimerUi? = null,
    var blackTimer: TimerUi? = null
) {
    val boardRotation: Float get() = match.getInitialBoardRotation()
    private val playerColor = match.getPlayerColor()
    val topTimer: TimerUi?
        get() {
            return when (playerColor) {
                PieceColor.Black -> whiteTimer
                PieceColor.White -> blackTimer
            }
        }
    val bottomTimer: TimerUi?
        get() = when (playerColor) {
            PieceColor.Black -> blackTimer
            PieceColor.White -> whiteTimer
        }
    val topBanner: GameBanner?
        get() = when (playerColor) {
            PieceColor.Black -> withesGameBanner
            PieceColor.White -> blacksGameBanner
        }
    val bottomBanner: GameBanner?
        get() = when (playerColor) {
            PieceColor.Black -> blacksGameBanner
            PieceColor.White -> withesGameBanner
        }
}

data class GameBanner(
    val takenPieces: Map<DrawableResource, TakenPiece>,
    val pieceColor: PieceColor,
    val advantageLabel: String,
    val playerId: String
)

data class TakenPiece(
    val order: Int,
    val count: Int
)

data class PromotionUiModel(
    val items: List<PromotionItem>
)

data class PromotionItem(
    val drawableResource: DrawableResource,
    val type: Promotion.Type,
    val color: PieceColor,
    val position: PiecePosition
)

data class TimerUi(
    val timeLeft: String,
    val backgroundColor: Color,
    val textColor: Color,
    val timerFontWeight: FontWeight
)