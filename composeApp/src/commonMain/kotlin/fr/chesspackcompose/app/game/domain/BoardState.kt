package fr.chesspackcompose.app.game.domain

import fr.chesspackcompose.app.game.domain.pieces.Piece

data class BoardState(
    val pieces: MutableSet<Piece> = mutableSetOf(),
    val currentPlayer: PieceColor = PieceColor.White,
    val soundEffect: SoundEffect? = null,
    val takenPieces: Map<PieceColor, MutableList<Piece>> = emptyMap(),
    val promotion: Promotion? = null,
    val winner: PieceColor? = null,
)