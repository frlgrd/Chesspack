package fr.chesspackcompose.app.game.domain.pieces

import fr.chesspackcompose.app.game.domain.PieceColor
import fr.chesspackcompose.app.game.domain.PiecePosition

data class Knight(
    override var position: PiecePosition,
    override val color: PieceColor,
) : Piece(position = position, color = color)