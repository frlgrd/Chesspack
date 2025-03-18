package fr.chesspackcompose.app.game.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import fr.chesspackcompose.app.game.domain.PiecePosition
import org.jetbrains.compose.resources.DrawableResource

@Immutable
data class CellUIModel(
    val position: PiecePosition,
    val squareColor: Color,
    val isChecked: Boolean,
    val isDragging: Boolean,
    val moveEnabled: Boolean = true,
    val markAsLegalMove: Boolean = false,
    val pieceInfo: PieceInfo? = null
) {
    val backgroundColor: Color = when {
        isDragging -> originalPositionColor
        isChecked -> checkmatedColor
        else -> squareColor
    }

    val contentDescription: String get() = position.toString()
}

data class PieceInfo(
    val drawableResource: DrawableResource,
    val legalMoves: List<PiecePosition>
)