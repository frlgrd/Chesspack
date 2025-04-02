package fr.chesspackcompose.app.game.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chesspackcompose.composeapp.generated.resources.Res
import chesspackcompose.composeapp.generated.resources.reset
import chesspackcompose.composeapp.generated.resources.rotate_mode_default
import fr.chesspackcompose.app.game.presentation.GameUiEvent
import fr.chesspackcompose.app.game.presentation.GameViewModel
import fr.chesspackcompose.app.game.presentation.ui.component.BoardUi
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GameScreen(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<GameViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier
            .background(Color.DarkGray)
            .safeDrawingPadding(),
        backgroundColor = Color.DarkGray,
        contentColor = Color.DarkGray,
        topBar = {
            TopAppBar(
                backgroundColor = Color.DarkGray,
                contentColor = Color.White,
                actions = {
                    IconButton(onClick = { viewModel.onEvent(GameUiEvent.ResetRequested) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(Res.string.reset)
                        )
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            onClick = {
                                viewModel.onEvent(GameUiEvent.SwitchRotateMode)
                                expanded = false
                            }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = state.sideBySide,
                                    onCheckedChange = null
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(text = stringResource(Res.string.rotate_mode_default))
                            }
                        }
                    }
                },
                title = { }
            )
        }) {
        BoardUi(state = state, onEvent = viewModel::onEvent)
    }
}