package fr.chesspackcompose.app

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import fr.chesspackcompose.app.core.di.initKoin

fun main() = application {
    LaunchedEffect(Unit) {
        initKoin()
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Chesspack Compose",
    ) {
        App()
    }
}