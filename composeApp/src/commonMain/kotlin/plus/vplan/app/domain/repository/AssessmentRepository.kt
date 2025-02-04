package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.model.VppId

interface AssessmentRepository {
    suspend fun download(
        schoolApiAccess: SchoolApiAccess,
        defaultLessonIds: List<Int>
    ): Response.Error?

    suspend fun createAssessmentOnline(
        vppId: VppId.Active,
        date: LocalDate,
        type: Assessment.Type,
        defaultLessonId: Int,
        isPublic: Boolean,
        content: String
    ): Response<Int>

    fun getAll(): Flow<List<Assessment>>
    fun getById(id: Int, forceReload: Boolean): Flow<CacheState<Assessment>>

    suspend fun getIdForNewLocalAssessment(): Int
    suspend fun upsert(assessments: List<Assessment>)
}