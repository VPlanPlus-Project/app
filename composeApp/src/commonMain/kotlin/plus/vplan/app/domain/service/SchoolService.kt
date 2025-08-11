package plus.vplan.app.domain.service

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.AliasState
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.model.School

interface SchoolService {
    fun getSchoolFromAlias(alias: Alias): Flow<AliasState<School>>
}