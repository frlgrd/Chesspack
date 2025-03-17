package fr.chesspackcompose.app.game.presentation

import androidx.compose.ui.graphics.Color
import fr.chesspackcompose.app.game.domain.PiecePosition
import org.jetbrains.compose.resources.DrawableResource

data class CellUIModel(
    val position: PiecePosition,
    val backgroundColor: Color,
    val moveEnabled: Boolean = true,
    val markAsLegalMove: Boolean = false,
    val pieceInfo: PieceInfo? = null
)

data class PieceInfo(
    val drawableResource: DrawableResource,
    val legalMoves: List<PiecePosition>
)