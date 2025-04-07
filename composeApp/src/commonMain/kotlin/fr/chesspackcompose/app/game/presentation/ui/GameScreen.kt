package fr.chesspackcompose.app.game.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import fr.chesspackcompose.app.GameRoute
import fr.chesspackcompose.app.game.presentation.GameViewModel
import fr.chesspackcompose.app.game.presentation.ui.component.BoardUi
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.game() {
    composable<GameRoute> {
        GameScreen()
    }
}

@Composable
fun GameScreen() {
    val viewModel = koinViewModel<GameViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    BoardUi(state = state, onEvent = viewModel::onEvent)
}