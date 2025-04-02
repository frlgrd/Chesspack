package fr.chesspackcompose.app.game.presentation.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import fr.chesspackcompose.app.game.presentation.GameUIState
import fr.chesspackcompose.app.game.presentation.GameUiEvent
import fr.chesspackcompose.app.game.presentation.RotateMode

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
            animationSpec = tween()
        ) {
            currentRotation = value
        }
    }

    if (state.promotionUiModel != null) {
        PromotionDialog(promotion = state.promotionUiModel, onEvent = onEvent)
    }
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        if (state.rotateMode == RotateMode.FaceToFace) {
            Divider()
            GameBannerUi(
                modifier = Modifier.rotate(180F),
                gameBanner = state.withesGameBanner,
                timerUi = state.whiteTimer
            )
            Divider()
            GameBannerUi(
                modifier = Modifier.rotate(180F),
                gameBanner = state.blacksGameBanner,
                timerUi = state.blackTimer
            )
        }
        Box(
            modifier = Modifier
                .onGloballyPositioned { squareSize = it.size.div(8).width.div(density.density).dp }
                .aspectRatio(1F)
                .graphicsLayer {
                    if (state.sideBySide) rotationZ = currentRotation
                }
        ) {
            state.cells.forEach { cell ->
                BoardCellUi(
                    cell = cell,
                    size = squareSize,
                    rotation = -currentRotation,
                    onEvent = onEvent
                )
            }
        }
        GameBannerUi(
            gameBanner = state.blacksGameBanner,
            timerUi = state.blackTimer
        )
        Divider()
        GameBannerUi(
            gameBanner = state.withesGameBanner,
            timerUi = state.whiteTimer
        )
        Divider()
    }
}
