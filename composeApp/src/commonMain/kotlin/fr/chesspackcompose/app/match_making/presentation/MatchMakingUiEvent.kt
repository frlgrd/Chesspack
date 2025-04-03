package fr.chesspackcompose.app.match_making.presentation

sealed interface MatchMakingUiEvent {
    data object Start : MatchMakingUiEvent
}