package fr.chesspackcompose.app.match_making

import fr.chesspackcompose.app.match_making.data.MatchMakingRepositoryImpl
import fr.chesspackcompose.app.match_making.domain.MatchMakingRepository
import fr.chesspackcompose.app.match_making.presentation.MatchMakingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val matchMakingFeatureModule = module {
    single<MatchMakingRepository> {
        MatchMakingRepositoryImpl(
            client = get()
        )
    }
    viewModelOf(::MatchMakingViewModel)
}