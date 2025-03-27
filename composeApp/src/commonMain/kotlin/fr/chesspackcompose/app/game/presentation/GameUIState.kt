package fr.chesspackcompose.app.game.presentation

import fr.chesspackcompose.app.game.domain.PieceColor

data class GameUIState(
    val cells: List<CellUIModel> = emptyList(),
    val boardRotation: Float = 0F,
    val withesGameBanner: GameBanner? = null,
    val blacksGameBanner: GameBanner? = null,
    val promotionUiModel: PromotionUiModel? = null,
    val currentPlayer: PieceColor = PieceColor.White,
    val winner: PieceColor? = null,
    val rotateMode: RotateMode = RotateMode.SideBySide
) {
    val gameFinished: Boolean get() = winner != null
    val sideBySide: Boolean get() = rotateMode == RotateMode.SideBySide
}