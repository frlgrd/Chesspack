package fr.chesspackcompose.app.game

import fr.chesspackcompose.app.game.data.BoardImpl
import fr.chesspackcompose.app.game.data.Fen
import fr.chesspackcompose.app.game.domain.Board
import fr.chesspackcompose.app.game.presentation.GameViewModel
import fr.chesspackcompose.app.game.presentation.PiecesMapper
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val gameModule = module {
    single<Fen> { Fen() }
    single<Board> { BoardImpl(fen = get()) }
    single<PiecesMapper> { PiecesMapper() }
    viewModelOf(::GameViewModel)
}