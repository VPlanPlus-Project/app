package plus.vplan.app.network.vpp.subject_instance

import plus.vplan.app.core.model.Alias
import plus.vplan.app.network.vpp.AliasDto

interface SubjectInstanceApi {
    suspend fun getByAlias(identifier: Alias): SubjectInstanceDto?
}

data class SubjectInstanceDto(
    val id: Int,
    val aliases: List<AliasDto>
)