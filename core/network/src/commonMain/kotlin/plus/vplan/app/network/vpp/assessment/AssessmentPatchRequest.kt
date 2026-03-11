package plus.vplan.app.network.vpp.assessment

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.core.utils.Optional

@Serializable
data class AssessmentPatchRequest(
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("type") val type: Optional<String> = Optional.Undefined(),
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("date") val date: Optional<String> = Optional.Undefined(),
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("is_public") val isPublic: Optional<Boolean> = Optional.Undefined(),
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("content") val content: Optional<String> = Optional.Undefined(),
)
