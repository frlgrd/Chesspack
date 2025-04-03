package fr.chesspackcompose.app.match_making.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import fr.chesspackcompose.app.NavigationDestination
import fr.chesspackcompose.app.match_making.presentation.MatchMakingUiEvent
import fr.chesspackcompose.app.match_making.presentation.MatchMakingViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.matchMaking() {
    composable<NavigationDestination.MatchMaking> {
        MatchMakingScreen()
    }
}

@Composable
fun MatchMakingScreen() {
    val viewModel = koinViewModel<MatchMakingViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { viewModel.onEvent(MatchMakingUiEvent.Start) },
            enabled = state.buttonEnabled
        ) {
            Text(state.text)
        }
        if (state.matchMakingStatus != null) {
            Text(state.matchMakingStatus.toString())
        }
    }
}