package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.SchoolApiAccess
import kotlin.uuid.Uuid

interface SubjectInstanceRepository {
    fun getAll(): Flow<List<SubjectInstance>>
    fun getByGroup(groupId: Uuid): Flow<List<SubjectInstance>>
    fun getBySchool(schoolId: Uuid, forceReload: Boolean): Flow<List<SubjectInstance>>
    fun getById(id: Int, forceReload: Boolean): Flow<CacheStateOld<SubjectInstance>>
    fun lookupBySp24Id(indiwareId: String): Flow<CacheStateOld<SubjectInstance>>

    suspend fun download(schoolId: Uuid, schoolApiAccess: SchoolApiAccess): Response<List<SubjectInstance>>

    suspend fun upsert(subjectInstance: SubjectInstance): SubjectInstance
    suspend fun upsert(subjectInstances: List<SubjectInstance>)
    suspend fun deleteById(id: Int)
    suspend fun deleteById(ids: List<Int>)
}