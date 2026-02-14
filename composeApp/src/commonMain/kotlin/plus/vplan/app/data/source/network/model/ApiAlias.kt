package plus.vplan.app.data.source.network.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider

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
    fun toModel() = Alias(
        provider = AliasProvider.valueOf(type.name),
        value = alias,
        version = version
    )
}