package fr.chesspackcompose.app.core.di

import fr.chesspackcompose.app.core.audio.audioModule
import fr.chesspackcompose.app.core.network.di.networkModule
import fr.chesspackcompose.app.game.gameModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(audioModule, networkModule, gameModule)
    }
}