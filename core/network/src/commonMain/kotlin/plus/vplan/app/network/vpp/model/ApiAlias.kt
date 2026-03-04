package plus.vplan.app.network.vpp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.network.vpp.AliasDto

@Serializable
enum class ApiAliasType(val apiName: String) {
    @SerialName("sp24") Sp24("sp24"),
    @SerialName("vpp") Vpp("vpp"),
    @SerialName("schulverwalter") Schulverwalter("schulverwalter")
}

@Serializable
data class ApiAlias(
    @SerialName("type") val type: ApiAliasType,
    @SerialName("value") val alias: String,
    @SerialName("version") val version: Int
) {
    fun toDto() = AliasDto(
        type = type.apiName,
        value = alias,
        version = version
    )
}