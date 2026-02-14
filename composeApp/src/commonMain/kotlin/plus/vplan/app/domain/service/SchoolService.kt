package plus.vplan.app.domain.service

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.AliasState
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.School

interface SchoolService {
    fun getSchoolFromAlias(alias: Alias): Flow<AliasState<School>>
}