package plus.vplan.app.network.vpp.assessment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssessmentPostRequest(
    @SerialName("subject_instance_id") val subjectInstanceId: Int,
    @SerialName("date") val date: String,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("content") val content: String,
    @SerialName("type") val type: String
)

@Serializable
data class AssessmentPostResponse(
    @SerialName("id") val id: Int
)
