package fr.chesspackcompose.app.core.audio

interface SoundEffectPlayer {
    fun play(uri: String)
    fun release()
}