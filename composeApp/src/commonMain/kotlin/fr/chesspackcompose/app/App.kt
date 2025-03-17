package fr.chesspackcompose.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.chesspackcompose.app.game.gameModule
import fr.chesspackcompose.app.game.presentation.GameViewModel
import fr.chesspackcompose.app.game.presentation.ui.BoardUi
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    KoinApplication(application = { modules(gameModule) }) {
        MaterialTheme {
            val viewModel = koinViewModel<GameViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            Box(
                modifier = Modifier.fillMaxSize().background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                BoardUi(state = state, onEvent = viewModel::onEvent)
            }
        }
    }
}