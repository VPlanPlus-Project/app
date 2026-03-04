package plus.vplan.app.network.vpp

import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider

data class AliasDto(
    val type: String,
    val value: String,
    val version: Int,
) {
    fun toModel(): Alias? {
        return Alias(
            provider = when (this.type) {
                "vpp" -> AliasProvider.Vpp
                "sp24" -> AliasProvider.Sp24
                "schulverwalter" -> AliasProvider.Schulverwalter
                else -> return null
            },
            value = this.value,
            version = this.version
        )
    }
}