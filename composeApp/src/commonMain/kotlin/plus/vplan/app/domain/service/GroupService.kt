package plus.vplan.app.domain.service

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.AliasState
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.model.Group

interface GroupService {
    fun getGroupFromAlias(alias: Alias): Flow<AliasState<Group>>
}