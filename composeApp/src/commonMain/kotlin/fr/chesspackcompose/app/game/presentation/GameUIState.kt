package fr.chesspackcompose.app.game.presentation

data class GameUIState(
    val cells: List<CellUIModel> = emptyList(),
    val boardRotation: Float = 0F,
    val withesTaken: TakenPieces? = null,
    val blacksTaken: TakenPieces? = null,
) {
    val topTaken = if (boardRotation == 0F) {
        withesTaken
    } else {
        blacksTaken
    }
    val bottomTaken = if (boardRotation == 0F) {
        blacksTaken
    } else {
        withesTaken
    }
}