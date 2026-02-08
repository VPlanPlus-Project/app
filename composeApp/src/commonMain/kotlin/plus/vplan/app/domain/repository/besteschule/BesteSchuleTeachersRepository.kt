package plus.vplan.app.domain.repository.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.besteschule.BesteSchuleTeacher
import plus.vplan.app.domain.model.besteschule.api.ApiStudentGradesData
import plus.vplan.app.domain.repository.base.PrefetchInstruction
import plus.vplan.app.domain.repository.base.PrefetchResult
import plus.vplan.app.domain.repository.base.PrefetchableRepository
import plus.vplan.app.domain.repository.base.ResponsePreference

interface BesteSchuleTeachersRepository : PrefetchableRepository {
    suspend fun getTeachersFromApi(schulverwalterAccessToken: String): Response<List<ApiStudentGradesData.Teacher>>
    suspend fun addTeachersToCache(items: List<BesteSchuleTeacher>)
    fun getTeacherFromCache(teacherId: Int): Flow<BesteSchuleTeacher?>

    /**
     * @param includes Map of relationship names to PrefetchInstruction configurations for prefetching related entities
     */
    fun getTeachers(
        responsePreference: ResponsePreference,
        contextBesteschuleAccessToken: String?,
        includes: Map<String, PrefetchInstruction> = emptyMap()
    ): Flow<Response<List<BesteSchuleTeacher>>>

    /**
     * Prefetch teachers by IDs. Used internally for relationship preloading.
     * @param includes Optional nested PrefetchInstructions for prefetching related entities
     * @return PrefetchResult with success count and any errors
     */
    suspend fun prefetchByIds(ids: List<Int>, includes: Map<String, PrefetchInstruction> = emptyMap()): PrefetchResult
}
