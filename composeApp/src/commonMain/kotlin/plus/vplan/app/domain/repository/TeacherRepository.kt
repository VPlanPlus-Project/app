package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Teacher

interface TeacherRepository: WebEntityRepository<Teacher> {
    fun getBySchool(schoolId: Int): Flow<List<Teacher>>
    suspend fun getBySchoolWithCaching(school: School, forceReload: Boolean): Response<Flow<List<Teacher>>>
}