package fr.chesspackcompose.app.game.domain

import fr.chesspackcompose.app.game.domain.pieces.Pawn

data class Promotion(val pawn: Pawn) {
    enum class Type {
        QUEEN, ROOK, BISHOP, KNIGHT
    }
}