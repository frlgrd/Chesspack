package fr.chesspackcompose.app.match_making.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import fr.chesspackcompose.app.NavigationDestination

fun NavGraphBuilder.matchMaking() {
    composable<NavigationDestination.MatchMaking> {
        MatchMakingScreen()
    }
}

@Composable
fun MatchMakingScreen(modifier: Modifier = Modifier) {

}