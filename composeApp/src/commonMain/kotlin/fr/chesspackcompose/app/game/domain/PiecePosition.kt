package fr.chesspackcompose.app.game.domain

data class PiecePosition(val x: Int, val y: Int)

private const val HORIZONTAL_COORDINATES = "abcdefgh"
val PiecePosition.pgnPositionY: String get() = "${HORIZONTAL_COORDINATES[x]}"
val PiecePosition.pgnPositionX: String get() = "${8 - y}"