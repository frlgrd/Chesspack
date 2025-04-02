package fr.chesspackcompose.app.game.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import fr.chesspackcompose.app.game.domain.PiecePosition
import fr.chesspackcompose.app.game.presentation.ui.checkmatedColor
import fr.chesspackcompose.app.game.presentation.ui.hoverColorWithAlpha
import fr.chesspackcompose.app.game.presentation.ui.originalPositionColorWithAlpha
import org.jetbrains.compose.resources.DrawableResource

@Immutable
data class CellUIModel(
    val position: PiecePosition,
    val squareColor: Color,
    val isChecked: Boolean = false,
    val isDragging: Boolean = false,
    val moveEnabled: Boolean = true,
    val markAsLegalMove: Boolean = false,
    val markAsHovered: Boolean = false,
    val pieceInfo: PieceInfo? = null,
    val coordinateUI: CoordinateUI? = null
) {
    private val originalPosition = originalPositionColorWithAlpha.compositeOver(squareColor)
    private val hoverColor = hoverColorWithAlpha.compositeOver(squareColor)

    val backgroundColor: Color = when {
        isDragging -> originalPosition
        isChecked -> checkmatedColor
        markAsHovered -> hoverColor
        else -> squareColor
    }

    val contentDescription: String get() = position.toString()
}

data class PieceInfo(
    val drawableResource: DrawableResource,
    val legalMoves: List<PiecePosition>
)

data class CoordinateUI(
    val color: Color,
    val x: String?,
    val y: String?
)
