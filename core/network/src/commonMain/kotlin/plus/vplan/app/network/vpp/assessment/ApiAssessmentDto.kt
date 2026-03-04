package plus.vplan.app.network.vpp.assessment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.network.vpp.model.IncludedModel

@Serializable
data class ApiAssessmentDto(
    @SerialName("id") val id: Int,
    @SerialName("subject_instance") val subject: IncludedModel,
    @SerialName("content") val description: String = "",
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("date") val date: String,
    @SerialName("type") val type: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("created_by") val createdBy: IncludedModel,
    @SerialName("files") val files: List<IncludedModel>
)
