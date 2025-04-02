package fr.chesspackcompose.app.game.presentation.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import fr.chesspackcompose.app.game.presentation.GameUiEvent
import fr.chesspackcompose.app.game.presentation.PromotionUiModel
import org.jetbrains.compose.resources.painterResource

@Composable
fun PromotionDialog(
    promotion: PromotionUiModel,
    onEvent: (GameUiEvent) -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Row {
            promotion.items.forEach { promotion ->
                Image(modifier = Modifier.clickable {
                    onEvent(GameUiEvent.OnPromotion(promotion))
                }, painter = painterResource(promotion.drawableResource), contentDescription = null)
            }
        }
    }
}