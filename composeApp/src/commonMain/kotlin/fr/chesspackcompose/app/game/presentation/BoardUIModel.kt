package fr.chesspackcompose.app.game.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
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
    val markAsHovered: Boolean = false,
    val pieceInfo: PieceInfo? = null
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

data class TakenPieces(
    val pieces: Map<DrawableResource, TakenPiece>,
    val advantageLabel: String?
)

data class TakenPiece(
    val order: Int,
    val count: Int
)
