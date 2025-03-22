package fr.chesspackcompose.app.core.audio

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSURL

actual class SoundEffectPlayerImpl : SoundEffectPlayer {

    private var avAudioPlayer: AVAudioPlayer? = null

    @OptIn(ExperimentalForeignApi::class)
    override fun play(uri: String) {
        val url = NSURL.URLWithString(URLString = uri) ?: return
        avAudioPlayer = AVAudioPlayer(url, error = null)
        avAudioPlayer?.prepareToPlay()
        avAudioPlayer?.play()
    }

    override fun release() {
        avAudioPlayer?.stop()
    }
}