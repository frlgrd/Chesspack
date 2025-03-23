package fr.chesspackcompose.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import chesspackcompose.composeapp.generated.resources.Res
import chesspackcompose.composeapp.generated.resources.piece_pawn_side_black
import chesspackcompose.composeapp.generated.resources.piece_queen_side_black
import fr.chesspackcompose.app.game.domain.PieceColor
import fr.chesspackcompose.app.game.presentation.TakenPiece
import fr.chesspackcompose.app.game.presentation.ui.GameInfoUi

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    GameInfoUi(
        gameInfo = fr.chesspackcompose.app.game.presentation.GameInfo(
            takenPieces = mapOf(
                Res.drawable.piece_pawn_side_black to TakenPiece(order = 1, count = 3),
                Res.drawable.piece_queen_side_black to TakenPiece(order = 3, count = 1)
            ),
            pieceColor = PieceColor.White,
            textColor = Color.White,
            advantageLabel = "+10"
        ),
        currentPlayer = null,
        onEvent = {}
    )
}