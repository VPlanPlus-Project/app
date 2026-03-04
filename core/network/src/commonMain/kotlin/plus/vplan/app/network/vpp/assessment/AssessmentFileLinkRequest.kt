package plus.vplan.app.network.vpp.assessment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssessmentFileLinkRequest(
    @SerialName("file_id") val fileId: Int
)
