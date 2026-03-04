package plus.vplan.app.network.vpp.assessment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssessmentPatchRequest(
    @SerialName("type") val type: String? = null,
    @SerialName("date") val date: String? = null,
    @SerialName("is_public") val isPublic: Boolean? = null,
    @SerialName("content") val content: String? = null
)
