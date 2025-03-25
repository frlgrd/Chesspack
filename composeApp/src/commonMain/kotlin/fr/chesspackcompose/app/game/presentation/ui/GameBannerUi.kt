package fr.chesspackcompose.app.game.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.chesspackcompose.app.game.domain.PieceColor
import fr.chesspackcompose.app.game.presentation.GameBanner
import fr.chesspackcompose.app.game.presentation.GameUiEvent
import fr.chesspackcompose.app.game.presentation.TakenPiece
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun GameBannerUi(
    gameBanner: GameBanner?,
    currentPlayer: PieceColor,
    gameFinished: Boolean,
    onEvent: (GameUiEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (gameBanner != null) {
            gameBanner.takenPieces.entries.sortedBy { it.value.order }.forEach { taken ->
                TakenPiecesGroup(takenPiece = taken.value, image = taken.key)
            }
            Text(
                modifier = Modifier.padding(start = 6.dp).weight(1F),
                text = gameBanner.advantageLabel,
                color = gameBanner.textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Timer(
                gameBanner = gameBanner,
                currentPlayer = currentPlayer,
                gameFinished = gameFinished,
                onEvent = onEvent
            )
        }
    }
}

@Composable
private fun TakenPiecesGroup(
    takenPiece: TakenPiece,
    image: DrawableResource
) {
    Box {
        repeat(takenPiece.count) { index ->
            Image(
                modifier = Modifier.padding(start = 12.dp.times(index)).size(30.dp),
                painter = painterResource(image),
                contentDescription = null
            )
        }
    }
}