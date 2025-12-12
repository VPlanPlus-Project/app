package plus.vplan.app.domain.repository.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.besteschule.BesteSchuleTeacher
import plus.vplan.app.domain.model.besteschule.api.ApiStudentGradesData
import plus.vplan.app.domain.repository.base.ResponsePreference

interface BesteSchuleTeachersRepository {
    suspend fun getTeachersFromApi(schulverwalterAccessToken: String): Response<List<ApiStudentGradesData.Teacher>>
    suspend fun addTeachersToCache(items: List<BesteSchuleTeacher>)
    fun getTeacherFromCache(teacherId: Int): Flow<BesteSchuleTeacher?>

    fun getTeachers(
        responsePreference: ResponsePreference,
        contextBesteschuleAccessToken: String?,
    ): Flow<Response<List<BesteSchuleTeacher>>>
}