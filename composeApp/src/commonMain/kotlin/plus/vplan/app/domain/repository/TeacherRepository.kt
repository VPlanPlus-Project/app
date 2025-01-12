package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Teacher

interface TeacherRepository {
    fun getBySchool(schoolId: Int): Flow<List<Teacher>>
    suspend fun getBySchoolWithCaching(school: School): Response<Flow<List<Teacher>>>
    fun getById(teacherId: Int): Flow<CacheState<Teacher>>
    suspend fun getByIdWithCaching(id: Int, school: School): Response<Flow<Teacher?>>
}