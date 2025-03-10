package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.SchoolApiAccess

interface SubjectInstanceRepository {
    fun getAll(): Flow<List<SubjectInstance>>
    fun getByGroup(groupId: Int): Flow<List<SubjectInstance>>
    fun getBySchool(schoolId: Int, forceReload: Boolean): Flow<List<SubjectInstance>>
    fun getById(id: Int, forceReload: Boolean): Flow<CacheState<SubjectInstance>>
    fun getByIndiwareId(indiwareId: String): Flow<CacheState<SubjectInstance>>

    suspend fun download(schoolId: Int, schoolApiAccess: SchoolApiAccess): Response<List<SubjectInstance>>

    suspend fun upsert(subjectInstance: SubjectInstance): SubjectInstance
    suspend fun upsert(subjectInstances: List<SubjectInstance>)
    suspend fun deleteById(id: Int)
    suspend fun deleteById(ids: List<Int>)
}