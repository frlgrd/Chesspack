package fr.chesspackcompose.app.game.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import fr.chesspackcompose.app.NavigationDestination
import fr.chesspackcompose.app.game.presentation.GameViewModel
import fr.chesspackcompose.app.game.presentation.ui.component.BoardUi
import fr.chesspackcompose.app.match_making.domain.Match
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.game() {
    composable<NavigationDestination.Game> {
        val match = it.toRoute<NavigationDestination.Game>().match
        GameScreen(match)
    }
}

@Composable
fun GameScreen(match: Match) {
    val viewModel = koinViewModel<GameViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    BoardUi(state = state, onEvent = viewModel::onEvent)
}