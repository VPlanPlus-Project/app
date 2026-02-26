package plus.vplan.app.network.vpp.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.network.vpp.AliasDto

@Serializable
enum class ApiAliasType {
    @SerialName("sp24") Sp24,
    @SerialName("vpp") Vpp,
    @SerialName("schulverwalter") Schulverwalter
}

@Serializable
data class ApiAlias(
    @SerialName("type") val type: ApiAliasType,
    @SerialName("value") val alias: String,
    @SerialName("version") val version: Int
) {
    fun toDto() = AliasDto(
        type = type.name,
        value = alias,
        version = version
    )
}