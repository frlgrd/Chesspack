package fr.chesspackcompose.app.game.domain

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement

@Serializable(with = GameMoveSerializer::class)
sealed interface GameMove {
    @Serializable
    data class Move(
        val playerId: String,
        val from: PiecePosition,
        val to: PiecePosition
    ) : GameMove
}

object GameMoveSerializer : JsonContentPolymorphicSerializer<GameMove>(
    baseClass = GameMove::class
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<GameMove> {
        return GameMove.Move.serializer()
    }
}
