package fr.chesspackcompose.app.game.presentation

data class GameUIState(
    val cells: List<CellUIModel> = emptyList(),
    val boardRotation: Float = 0F,
    val withesGameInfo: GameInfo? = null,
    val blacksGameInfo: GameInfo? = null,
    val promotionUiModel: PromotionUiModel? = null,
    val canReset: Boolean = false
)