package fr.chesspackcompose.app.match_making.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import fr.chesspackcompose.app.MatchMakingRoute
import fr.chesspackcompose.app.match_making.domain.Match
import fr.chesspackcompose.app.match_making.presentation.MatchMakingUiEvent
import fr.chesspackcompose.app.match_making.presentation.MatchMakingViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.matchMaking(onMatchFound: (Match) -> Unit) {
    composable<MatchMakingRoute> {
        MatchMakingScreen(onMatchFound = onMatchFound)
    }
}

@Composable
fun MatchMakingScreen(onMatchFound: (Match) -> Unit) {
    val viewModel = koinViewModel<MatchMakingViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val match = remember(state.match) { state.match }
    var username by remember { mutableStateOf("") }

    if (match != null) {
        onMatchFound(match)
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = username,
            onValueChange = { username = it },
            placeholder = { Text("player") }
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.onEvent(MatchMakingUiEvent.Start(username)) },
            enabled = state.buttonEnabled
        ) {
            Text(state.text)
        }
    }
}