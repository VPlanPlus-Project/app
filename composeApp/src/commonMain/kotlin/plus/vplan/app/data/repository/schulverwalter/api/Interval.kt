package plus.vplan.app.data.repository.schulverwalter.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Interval(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("from") val from: String,
    @SerialName("to") val to: String,
    @SerialName("included_interval_id") val includedIntervalId: Int?,
    @SerialName("year_id") val yearId: Int
)