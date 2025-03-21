package fr.chesspackcompose.app.game.presentation

import fr.chesspackcompose.app.game.domain.PieceColor

data class GameUIState(
    val cells: List<CellUIModel> = emptyList(),
    val boardRotation: Float = 0F,
    val withesTaken: TakenPieces? = null,
    val blacksTaken: TakenPieces? = null,
    val winner: PieceColor? = null
)