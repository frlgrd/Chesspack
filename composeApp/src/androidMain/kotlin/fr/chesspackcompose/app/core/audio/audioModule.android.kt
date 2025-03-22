package fr.chesspackcompose.app.core.audio

import org.koin.core.module.Module
import org.koin.dsl.module

actual val audioModule: Module
    get() = module {
        single<SoundEffectPlayer> {
            SoundEffectPlayerImpl(
                context = get()
            )
        }
    }