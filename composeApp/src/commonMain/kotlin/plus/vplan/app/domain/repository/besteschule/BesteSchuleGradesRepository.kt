package plus.vplan.app.domain.repository.besteschule

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import plus.vplan.app.domain.model.besteschule.api.ApiStudentGradesData
import plus.vplan.app.domain.repository.base.PrefetchInstruction
import plus.vplan.app.domain.repository.base.PrefetchResult
import plus.vplan.app.domain.repository.base.PrefetchableRepository
import plus.vplan.app.domain.repository.base.ResponsePreference

interface BesteSchuleGradesRepository : PrefetchableRepository {
    suspend fun getGradesFromApi(schulverwalterAccessToken: String): Response<List<ApiStudentGradesData>>
    suspend fun getGradeFromApi(gradeId: Int, schulverwalterAccessToken: String): Response<ApiStudentGradesData>
    suspend fun addGradesToCache(items: List<BesteSchuleGrade>)
    fun getGradeFromCache(gradeId: Int): Flow<BesteSchuleGrade?>

    /**
     * @param includes Map of relationship names to PrefetchInstruction for prefetching related entities
     */
    fun getGrades(
        responsePreference: ResponsePreference,
        contextBesteschuleAccessToken: String?,
        contextBesteschuleUserId: Int?,
        filter: GradeFilter = GradeFilter(),
        includes: Map<String, PrefetchInstruction> = emptyMap()
    ): Flow<Response<List<BesteSchuleGrade>>>

    fun getGrade(
        gradeId: Int,
        responsePreference: ResponsePreference,
        contextBesteschuleAccessToken: String?,
        contextBesteschuleUserId: Int?
    ): Flow<Response<BesteSchuleGrade>>

    suspend fun clearCacheForUser(schulverwalterUserId: Int)

    /**
     * Prefetch grades by IDs. Used internally for relationship preloading.
     * @param includes Optional nested includes for prefetching related entities
     * @return PrefetchResult with success count and any errors
     */
    suspend fun prefetchByIds(ids: List<Int>, includes: Map<String, PrefetchInstruction> = emptyMap()): PrefetchResult
}

data class GradeFilter(
    val yearId: Int? = null,
    val intervalId: Int? = null,
    val collectionId: Int? = null,
    val subjectId: Int? = null,
    val teacherId: Int? = null,
    val givenAt: LocalDate? = null
)
