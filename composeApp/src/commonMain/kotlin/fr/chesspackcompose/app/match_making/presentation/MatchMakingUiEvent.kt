package fr.chesspackcompose.app.match_making.presentation

sealed interface MatchMakingUiEvent {
    data class Start(val playerId: String) : MatchMakingUiEvent
}