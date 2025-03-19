import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import fr.chesspackcompose.app.App
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class BasicMovesUiTests {
    @Test
    fun verifyPiecesPresence() = runComposeUiTest {
        setContent { App() }
        verifyBoard()
    }

    @Test
    fun verifyPawnsMove() = runComposeUiTest {
        setContent { App() }

        val rowWithPawns = listOf(1, 6)
        (0..7).forEach { x ->
            rowWithPawns.forEach { y ->
                onPiece(x, y)
                    .assertIsDisplayed()
                    .performTouchInput {
                        down(center)
                        moveBy(delta = Offset(0F, 45F), delayMillis = 1000)
                        up()
                        waitForIdle()
                    }
            }
        }
    }
}

private fun SemanticsNodeInteractionsProvider.verifyBoard() {
    onPiece(0, 0).assertIsDisplayed()
    onPiece(1, 0).assertIsDisplayed()
    onPiece(2, 0).assertIsDisplayed()
    onPiece(3, 0).assertIsDisplayed()
    onPiece(4, 0).assertIsDisplayed()
    onPiece(5, 0).assertIsDisplayed()
    onPiece(6, 0).assertIsDisplayed()
    onPiece(7, 0).assertIsDisplayed()
    onPiece(0, 1).assertIsDisplayed()
    onPiece(1, 1).assertIsDisplayed()
    onPiece(2, 1).assertIsDisplayed()
    onPiece(3, 1).assertIsDisplayed()
    onPiece(4, 1).assertIsDisplayed()
    onPiece(5, 1).assertIsDisplayed()
    onPiece(6, 1).assertIsDisplayed()
    onPiece(7, 1).assertIsDisplayed()
    onPiece(0, 6).assertIsDisplayed()
    onPiece(1, 6).assertIsDisplayed()
    onPiece(2, 6).assertIsDisplayed()
    onPiece(3, 6).assertIsDisplayed()
    onPiece(4, 6).assertIsDisplayed()
    onPiece(5, 6).assertIsDisplayed()
    onPiece(6, 6).assertIsDisplayed()
    onPiece(7, 6).assertIsDisplayed()
    onPiece(0, 7).assertIsDisplayed()
    onPiece(1, 7).assertIsDisplayed()
    onPiece(2, 7).assertIsDisplayed()
    onPiece(3, 7).assertIsDisplayed()
    onPiece(4, 7).assertIsDisplayed()
    onPiece(5, 7).assertIsDisplayed()
    onPiece(6, 7).assertIsDisplayed()
    onPiece(7, 7).assertIsDisplayed()
}

private fun SemanticsNodeInteractionsProvider.onPiece(
    x: Int,
    y: Int
): SemanticsNodeInteraction {
    return onNodeWithContentDescription("PiecePosition(x=$x, y=$y)")
}