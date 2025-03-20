package fr.chesspackcompose.app.game.domain

enum class PieceColor {
    Black, White;

    fun switch() = when (this) {
        Black -> White
        White -> Black
    }
}
