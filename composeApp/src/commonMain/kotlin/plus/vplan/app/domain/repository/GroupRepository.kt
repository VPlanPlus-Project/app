package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.School

interface GroupRepository {
    fun getBySchool(schoolId: Int): Flow<List<Group>>
    suspend fun getBySchoolWithCaching(school: School): Response<Flow<List<Group>>>
    fun getById(id: Int): Flow<CacheState<Group>>
    suspend fun getByIdWithCaching(id: Int, school: School): Response<Flow<Group?>>
}