package plus.vplan.app.domain.service

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.AliasState
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.AliasProvider
import plus.vplan.app.domain.model.SubjectInstance

interface SubjectInstanceService {
    fun getSubjectInstanceFromAlias(alias: Alias, forceUpdate: Boolean = false): Flow<AliasState<SubjectInstance>>
    suspend fun findAliasForSubjectInstance(subjectInstance: SubjectInstance, aliasProvider: AliasProvider): Alias?
}