package fr.chesspackcompose.app.game.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.chesspackcompose.app.game.domain.PieceColor
import fr.chesspackcompose.app.game.presentation.GameInfo
import fr.chesspackcompose.app.game.presentation.GameUiEvent
import kotlinx.coroutines.delay

private val defaultBackgroundColor = Color.White.copy(alpha = 0.2F)
private val alphaRedBackgroundColor = Color.Red.copy(alpha = 0.2F)
private val redBackgroundColor = Color.Red

@Composable
fun Timer(
    gameInfo: GameInfo,
    currentPlayer: PieceColor?,
    onEvent: (GameUiEvent) -> Unit
) {
    var leftTime by remember { mutableStateOf(10 * 60 * 1000) }
    var step by remember { mutableStateOf(1000) }
    val backgroundColor = remember(gameInfo.textColor, leftTime) {
        when {
            leftTime == 0 -> redBackgroundColor
            leftTime < 10 * 1000 -> alphaRedBackgroundColor
            else -> defaultBackgroundColor
        }
    }
    LaunchedEffect(currentPlayer) {
        while (currentPlayer == gameInfo.pieceColor && leftTime > 0) {
            delay(step.toLong())
            leftTime -= step
            if (leftTime < 10 * 1000 && step == 1000) {
                step = 100
            }
            if (leftTime == 0) {
                onEvent(GameUiEvent.TimerFinished(gameInfo.pieceColor))
            }
        }
    }
    Text(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 4.dp),
        text = leftTime.formattedLeftTime(),
        color = gameInfo.textColor,
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

private fun Int.formattedLeftTime(): String {
    val seconds = (this % 60000) / 1000
    return if (this < 10 * 1000) {
        val decaseconds = (this % 600) / 100
        "${seconds.withDecimals()}:$decaseconds"
    } else {
        val minutes = (this / 60000)
        "${minutes.withDecimals()}:${seconds.withDecimals()}"
    }
}

private fun Int.withDecimals(): String {
    return if (this < 10) {
        "0$this"
    } else {
        toString()
    }
}
