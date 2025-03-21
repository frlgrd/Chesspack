package fr.chesspackcompose.app.game.presentation

data class GameUIState(
    val cells: List<CellUIModel> = emptyList(),
    val boardRotation: Float = 0F,
    val withesTaken: TakenPieces? = null,
    val blacksTaken: TakenPieces? = null
)