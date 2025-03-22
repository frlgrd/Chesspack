package fr.chesspackcompose.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chesspackcompose.composeapp.generated.resources.Res
import chesspackcompose.composeapp.generated.resources.app_name
import chesspackcompose.composeapp.generated.resources.reset
import fr.chesspackcompose.app.game.presentation.GameUiEvent
import fr.chesspackcompose.app.game.presentation.GameViewModel
import fr.chesspackcompose.app.game.presentation.ui.BoardUi
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    MaterialTheme {
        val viewModel = koinViewModel<GameViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        Scaffold(
            modifier = Modifier
                .background(Color.DarkGray)
                .safeDrawingPadding(),
            backgroundColor = Color.DarkGray,
            topBar = {
                TopAppBar(
                    backgroundColor = Color.DarkGray,
                    actions = {
                        if (state.canReset) {
                            IconButton(onClick = { viewModel.onEvent(GameUiEvent.ResetRequested) }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(Res.string.reset)
                                )
                            }
                        }
                    },
                    title = { Text(stringResource(Res.string.app_name)) }
                )
            }) {
            BoardUi(state = state, onEvent = viewModel::onEvent)
        }
    }
}
