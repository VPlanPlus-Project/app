package plus.vplan.app.domain.model.besteschule.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiStudentData(
    @SerialName("id") val id: Int,
    @SerialName("subjects") val subjects: List<Subject>,
    @SerialName("intervals") val intervals: List<Interval>,
) {
    @Serializable
    data class Subject(
        @SerialName("id") val id: Int,
        @SerialName("local_id") val shortName: String,
        @SerialName("name") val fullName: String
    )

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
}