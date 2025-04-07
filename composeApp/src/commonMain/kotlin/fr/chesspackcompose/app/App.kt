package fr.chesspackcompose.app

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import fr.chesspackcompose.app.game.presentation.ui.game
import fr.chesspackcompose.app.match_making.presentation.ui.matchMaking
import fr.chesspackcompose.app.ui.ChesspackTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Composable
fun App() {
    ChesspackTheme {
        Scaffold(modifier = Modifier.safeDrawingPadding()) {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = MatchMakingRoute) {
                matchMaking(onMatchFound = { match ->
                    navController.navigate(GameRoute(Json.encodeToString(match)))
                })
                game()
            }
        }
    }
}

@Serializable
object MatchMakingRoute

@Serializable
data class GameRoute(val matchJson: String)

