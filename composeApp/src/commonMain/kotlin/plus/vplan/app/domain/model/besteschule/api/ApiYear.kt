package plus.vplan.app.domain.model.besteschule.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiYear(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("from") val from: String,
    @SerialName("to") val to: String,
)