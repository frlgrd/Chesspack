package fr.chesspackcompose.app.game.presentation.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.chesspackcompose.app.game.presentation.GameUIState
import fr.chesspackcompose.app.game.presentation.GameUiEvent

@Composable
fun BoardUi(
    modifier: Modifier = Modifier,
    state: GameUIState,
    onEvent: (GameUiEvent) -> Unit
) {
    var squareSize by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    var currentRotation by remember { mutableStateOf(state.boardRotation) }
    val rotation = remember { Animatable(currentRotation) }
    LaunchedEffect(state.boardRotation) {
        rotation.animateTo(
            targetValue = state.boardRotation,
            animationSpec = tween(durationMillis = 700)
        ) {
            currentRotation = value
        }
    }
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        if (state.winner != null) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                text = "Winner is ${state.winner}"
            )
        }

        Box(
            modifier = Modifier
                .padding(vertical = 30.dp)
                .onGloballyPositioned { squareSize = it.size.div(8).width.div(density.density).dp }
                .aspectRatio(1F)
                .graphicsLayer { rotationZ = currentRotation }
        ) {
            state.cells.forEach { cell ->
                BoardCell(
                    cell = cell,
                    size = squareSize,
                    rotation = -currentRotation,
                    onEvent = onEvent
                )
            }
        }
        Column(modifier = Modifier) {
            TakenPieces(takenPieces = state.withesTaken)
            TakenPieces(takenPieces = state.blacksTaken)
        }
    }
}
