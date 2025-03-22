package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.School

interface GroupRepository: WebEntityRepository<Group> {
    fun getBySchool(schoolId: Int): Flow<List<Group>>
    suspend fun getBySchoolWithCaching(school: School, forceReload: Boolean = false): Response<Flow<List<Group>>>
}