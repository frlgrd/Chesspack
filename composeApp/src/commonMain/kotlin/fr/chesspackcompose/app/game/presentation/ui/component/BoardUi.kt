package fr.chesspackcompose.app.game.presentation.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import fr.chesspackcompose.app.game.presentation.GameUIState
import fr.chesspackcompose.app.game.presentation.GameUiEvent

@Composable
fun BoardUi(
    modifier: Modifier = Modifier,
    state: GameUIState,
    onEvent: (GameUiEvent) -> Unit
) {
    var cellSize by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    if (state.promotionUiModel != null) {
        PromotionDialog(promotion = state.promotionUiModel, onEvent = onEvent)
    }
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Divider()
        GameBannerUi(
            gameBanner = state.topBanner,
            timerUi = state.topTimer
        )
        Box(
            modifier = Modifier
                .onGloballyPositioned { cellSize = it.size.div(8).width.div(density.density).dp }
                .aspectRatio(1F)
                .rotate(state.boardRotation)
        ) {
            state.cells.forEach { cell ->
                BoardCellUi(
                    cell = cell,
                    size = cellSize,
                    rotation = state.boardRotation,
                    onEvent = onEvent
                )
            }
        }
        GameBannerUi(
            gameBanner = state.bottomBanner,
            timerUi = state.bottomTimer
        )
        Divider()
    }
}
