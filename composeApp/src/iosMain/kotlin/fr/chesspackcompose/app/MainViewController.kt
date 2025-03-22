package fr.chesspackcompose.app

import androidx.compose.ui.window.ComposeUIViewController
import fr.chesspackcompose.app.core.di.initKoin

@Suppress("FunctionName", "unused")
fun MainViewController() = ComposeUIViewController(
    configure = { initKoin() }
) { App() }