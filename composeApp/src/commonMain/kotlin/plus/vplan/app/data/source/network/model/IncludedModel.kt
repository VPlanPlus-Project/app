package plus.vplan.app.data.source.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IncludedModel(
    @SerialName("id") val id: Int
)