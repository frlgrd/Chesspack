package fr.chesspackcompose.app.game.presentation

import fr.chesspackcompose.app.game.domain.PieceColor

data class GameUIState(
    val cells: List<CellUIModel> = emptyList(),
    val boardRotation: Float = 0F,
    val withesGameInfo: GameInfo? = null,
    val blacksGameInfo: GameInfo? = null,
    val promotionUiModel: PromotionUiModel? = null,
    val canReset: Boolean = false,
    val currentPlayer: PieceColor = PieceColor.White,
    val winner: PieceColor? = null
) {
    val gameFinished: Boolean get() = winner != null
}