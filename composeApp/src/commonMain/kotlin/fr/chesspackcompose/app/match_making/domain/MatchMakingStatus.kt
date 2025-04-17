package fr.chesspackcompose.app.match_making.domain

import fr.chesspackcompose.app.game.domain.PieceColor
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement

@Serializable(with = MatchMakingSerializer::class)
sealed interface MatchMakingStatus {
    @Serializable
    data class MatchMakingInProgress(val progress: Int) : MatchMakingStatus

    @Serializable
    data class Done(
        val player1: MatchPlayer,
        val player2: MatchPlayer,
        val gameId: String
    ) : MatchMakingStatus
}

@Serializable
data class MatchPlayer(
    val id: String,
    val color: PieceColor
)


object MatchMakingSerializer : JsonContentPolymorphicSerializer<MatchMakingStatus>(
    baseClass = MatchMakingStatus::class
) {
    private const val PROGRESS = "progress"
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<MatchMakingStatus> {
        return if (element.toString().contains(PROGRESS)) {
            MatchMakingStatus.MatchMakingInProgress.serializer()
        } else {
            MatchMakingStatus.Done.serializer()
        }
    }
}