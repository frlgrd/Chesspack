package fr.chesspackcompose.app.game.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.chesspackcompose.app.game.presentation.TakenPiece
import fr.chesspackcompose.app.game.presentation.TakenPieces
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun TakenPieces(
    takenPieces: TakenPieces
) {

    Row(verticalAlignment = Alignment.CenterVertically) {
        takenPieces.pieces.entries.sortedBy { it.value.order }.forEach { taken ->
            TakenPiecesGroup(takenPiece = taken.value, image = taken.key)
        }
        if (takenPieces.advantage != null) {
            Text(
                modifier = Modifier.padding(start = 3.dp),
                text = "+${takenPieces.advantage}",
                color = Color.White
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
                modifier = Modifier.padding(start = 5.dp.times(index)).size(20.dp),
                painter = painterResource(image),
                contentDescription = null
            )
        }
    }
}