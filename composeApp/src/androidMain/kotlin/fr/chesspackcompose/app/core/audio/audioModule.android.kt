package fr.chesspackcompose.app.core.audio

import androidx.media3.exoplayer.ExoPlayer
import org.koin.core.module.Module
import org.koin.dsl.module

actual val audioModule: Module
    get() = module {
        single<ExoPlayer> {
            ExoPlayer.Builder(get())
                .build().apply {
                    playWhenReady = true
                }
        }
        single<SoundEffectPlayer> { SoundEffectPlayerImpl(player = get()) }
    }