package fr.chesspackcompose.app.game.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.chesspackcompose.app.game.presentation.TimerUi

@Composable
fun TimerText(timerUi: TimerUi) {
    Text(
        modifier = Modifier
            .background(color = timerUi.backgroundColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp),
        text = timerUi.timeLeft,
        color = timerUi.textColor,
        fontSize = 18.sp,
        fontWeight = timerUi.timerFontWeight
    )
}
