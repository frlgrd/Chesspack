package fr.chesspackcompose.app.game.domain

sealed interface SoundEffect {
    data object SimpleMove : SoundEffect
    data object Capture : SoundEffect
    data object Check : SoundEffect
    data object Checkmate : SoundEffect
    data object Castling : SoundEffect
}