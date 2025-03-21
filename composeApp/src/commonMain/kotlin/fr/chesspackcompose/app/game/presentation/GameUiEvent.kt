package fr.chesspackcompose.app.game.presentation

import fr.chesspackcompose.app.game.domain.PiecePosition

sealed interface GameUiEvent {
    data class PiecePicked(val cell: CellUIModel) : GameUiEvent
    data class PieceDropped(val cell: CellUIModel, val at: PiecePosition) : GameUiEvent
    data class PieceDragging(val at: PiecePosition) : GameUiEvent
    data object DragCanceled : GameUiEvent
    data class OnPromotion(val promotionItem: PromotionItem) : GameUiEvent
}