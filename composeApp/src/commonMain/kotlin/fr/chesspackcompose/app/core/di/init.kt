package fr.chesspackcompose.app.core.di

import fr.chesspackcompose.app.game.gameFeatureModule
import fr.chesspackcompose.app.match_making.matchMakingFeatureModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            matchMakingFeatureModule,
            gameFeatureModule
        )
    }
}