package fr.chesspackcompose.app.core.network.event

sealed interface WSEvent {
    sealed interface Service : WSEvent {
        data object SessionStartRequested : Service
        data object SessionStarted : Service
        data object SessionClosed : Service
    }

    sealed interface MatchMaking : WSEvent {
        data object Searching : MatchMaking
    }

    sealed interface Game : WSEvent {

    }
}