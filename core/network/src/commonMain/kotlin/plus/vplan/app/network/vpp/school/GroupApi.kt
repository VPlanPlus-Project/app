package plus.vplan.app.network.vpp.school

import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.network.vpp.AliasDto

interface GroupApi {
    suspend fun getById(identifier: Alias): VppGroupDto?
}

data class VppGroupDto(
    val id: Int,
    val name: String,
    val schoolId: Int,
    val aliases: List<AliasDto>
)