package fr.chesspackcompose.app.match_making.data

import fr.chesspackcompose.app.match_making.domain.MatchMakingStatus
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement

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
