package fr.chesspackcompose.app.game.domain.pieces

import fr.chesspackcompose.app.game.domain.PieceColor
import fr.chesspackcompose.app.game.domain.PiecePosition

data class King(
    override var position: PiecePosition,
    override val color: PieceColor,
) : Piece(position = position, color = color) {
    var isChecked = false
        private set

    fun updateCheck(isChecked: Boolean) {
        this.isChecked = isChecked
    }
}