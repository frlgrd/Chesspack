package fr.chesspackcompose.app.game.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import fr.chesspackcompose.app.game.domain.PieceColor
import fr.chesspackcompose.app.game.domain.PiecePosition
import fr.chesspackcompose.app.game.domain.Promotion
import org.jetbrains.compose.resources.DrawableResource

data class GameUIState(
    val cells: List<CellUIModel> = emptyList(),
    val boardRotation: Float = 0F,
    val withesGameBanner: GameBanner? = null,
    val blacksGameBanner: GameBanner? = null,
    val promotionUiModel: PromotionUiModel? = null,
    val currentPlayer: PieceColor = PieceColor.White,
    val winner: PieceColor? = null,
    val rotateMode: RotateMode = RotateMode.SideBySide,
    var whiteTimer: TimerUi? = null,
    var blackTimer: TimerUi? = null
) {
    val gameFinished: Boolean get() = winner != null
    val sideBySide: Boolean get() = rotateMode == RotateMode.SideBySide
}

data class GameBanner(
    val takenPieces: Map<DrawableResource, TakenPiece>,
    val pieceColor: PieceColor,
    val textColor: Color,
    val advantageLabel: String
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

enum class RotateMode {
    SideBySide, FaceToFace;

    fun switch(): RotateMode = when (this) {
        SideBySide -> FaceToFace
        FaceToFace -> SideBySide
    }
}

data class TimerUi(
    val timeLeft: String,
    val backgroundColor: Color,
    val textColor: Color,
    val timerFontWeight: FontWeight
)