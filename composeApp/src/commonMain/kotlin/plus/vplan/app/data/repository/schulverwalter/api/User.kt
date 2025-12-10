package plus.vplan.app.data.repository.schulverwalter.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val year: Year
) {
    @Serializable
    data class Year(
        @SerialName("intervals") val intervals: List<Interval>
    )
}