package plus.vplan.app.network.besteschule

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseDataWrapper<T>(
    @SerialName("data") val data: T
)