package fr.chesspackcompose.app.game.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import fr.chesspackcompose.app.NavigationDestination
import fr.chesspackcompose.app.game.presentation.GameUIState
import fr.chesspackcompose.app.game.presentation.GameUiEvent
import fr.chesspackcompose.app.game.presentation.GameViewModel
import fr.chesspackcompose.app.game.presentation.ui.component.BoardUi
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.game() {
    composable<NavigationDestination.Game> {
        val viewModel = koinViewModel<GameViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        GameScreen(
            onEvent = viewModel::onEvent,
            state = state
        )
    }
}

@Composable
fun GameScreen(
    onEvent: (GameUiEvent) -> Unit,
    state: GameUIState
) {
    BoardUi(state = state, onEvent = onEvent)
}