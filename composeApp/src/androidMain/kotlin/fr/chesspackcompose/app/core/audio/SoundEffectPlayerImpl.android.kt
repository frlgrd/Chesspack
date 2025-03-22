package fr.chesspackcompose.app.core.audio

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

actual class SoundEffectPlayerImpl(
    private val player: ExoPlayer
) : SoundEffectPlayer {

    private val mediaItems = mutableMapOf<String, MediaItem>()

    init {
        player.prepare()
    }

    override fun play(uri: String) {
        val mediaItem = mediaItems.getOrPut(uri) { MediaItem.fromUri(uri) }
        player.setMediaItem(mediaItem)
    }

    override fun release() {
        player.release()
    }
}