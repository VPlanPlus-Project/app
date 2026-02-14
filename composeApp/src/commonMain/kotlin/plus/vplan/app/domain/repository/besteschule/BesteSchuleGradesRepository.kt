package plus.vplan.app.domain.repository.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import plus.vplan.app.domain.model.besteschule.api.ApiStudentGradesData
import plus.vplan.app.domain.repository.base.ResponsePreference

interface BesteSchuleGradesRepository {
    suspend fun getGradeFromApi(gradeId: Int, schulverwalterAccessToken: String): Response<ApiStudentGradesData>
    suspend fun addGradesToCache(items: List<BesteSchuleGrade>)
    fun getGradeFromCache(gradeId: Int): Flow<BesteSchuleGrade?>
    fun getGradesFromCache(userId: Int?): Flow<List<BesteSchuleGrade>>

    fun getGrade(
        gradeId: Int,
        responsePreference: ResponsePreference,
        contextBesteschuleAccessToken: String?,
        contextBesteschuleUserId: Int?
    ): Flow<Response<BesteSchuleGrade>>

    suspend fun clearCacheForUser(schulverwalterUserId: Int)
}