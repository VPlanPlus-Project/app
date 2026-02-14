package plus.vplan.app.domain.repository.besteschule

import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.model.besteschule.api.ApiStudentData
import plus.vplan.app.domain.model.besteschule.api.ApiStudentGradesData

interface BesteSchuleApiRepository {
    suspend fun getStudentData(schulverwalterAccessToken: String, withCache: Boolean = true): Response<ApiStudentData>
    suspend fun getStudentGradeData(schulverwalterAccessToken: String): Response<List<ApiStudentGradesData>>
    fun clearApiCache()

    /**
     * @param yearId If null, set to current
     */
    suspend fun setYearForUser(schulverwalterAccessToken: String, yearId: Int?): Response<Unit>
}