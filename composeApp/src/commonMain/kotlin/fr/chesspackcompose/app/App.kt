package fr.chesspackcompose.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import fr.chesspackcompose.app.game.presentation.ui.game
import fr.chesspackcompose.app.match_making.presentation.matchMaking
import kotlinx.serialization.Serializable

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        Scaffold(
            modifier = Modifier
                .background(Color.DarkGray)
                .safeDrawingPadding(),
            backgroundColor = Color.DarkGray,
            contentColor = Color.DarkGray
        ) {
            NavHost(
                navController = navController,
                startDestination = NavigationDestination.MatchMaking
            ) {
                matchMaking()
                game()
            }
        }
    }
}

sealed interface NavigationDestination {

    @Serializable
    data object MatchMaking : NavigationDestination

    @Serializable
    data object Game : NavigationDestination
}

