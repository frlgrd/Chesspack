package fr.chesspackcompose.app.core.ui

import androidx.compose.ui.Modifier

fun Modifier.conditional(
    condition: Boolean,
    ifTrue: Modifier.() -> Modifier
): Modifier = if (condition) {
    then(ifTrue(Modifier))
} else {
    this
}