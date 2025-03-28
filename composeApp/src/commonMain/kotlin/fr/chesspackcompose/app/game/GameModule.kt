package fr.chesspackcompose.app.game

import fr.chesspackcompose.app.game.data.BoardImpl
import fr.chesspackcompose.app.game.data.CountdownTimerImpl
import fr.chesspackcompose.app.game.data.Fen
import fr.chesspackcompose.app.game.domain.Board
import fr.chesspackcompose.app.game.domain.CountdownTimer
import fr.chesspackcompose.app.game.presentation.BoardMapper
import fr.chesspackcompose.app.game.presentation.GameViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val gameModule = module {
    single<Fen> { Fen() }
    single<Board> { BoardImpl(fen = get()) }
    single<BoardMapper> { BoardMapper() }
    factory<CountdownTimer> { CountdownTimerImpl() }
    viewModelOf(::GameViewModel)
}