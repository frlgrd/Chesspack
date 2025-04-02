package fr.chesspackcompose.app

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import fr.chesspackcompose.app.game.presentation.ui.GameScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        GameScreen()
    }
}

