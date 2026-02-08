package plus.vplan.app.domain.repository.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.besteschule.BesteSchuleSubject
import plus.vplan.app.domain.model.besteschule.api.ApiStudentData
import plus.vplan.app.domain.repository.base.PrefetchInstruction
import plus.vplan.app.domain.repository.base.PrefetchResult
import plus.vplan.app.domain.repository.base.PrefetchableRepository
import plus.vplan.app.domain.repository.base.ResponsePreference

interface BesteSchuleSubjectsRepository : PrefetchableRepository {
    suspend fun getSubjectsFromApi(schulverwalterAccessToken: String): Response<List<ApiStudentData.Subject>>

    suspend fun addSubjectsToCache(subjects: Set<BesteSchuleSubject>)
    fun getSubjectFromCache(subjectId: Int): Flow<BesteSchuleSubject?>

    fun getSubjects(
        responsePreference: ResponsePreference,
        contextBesteschuleAccessToken: String?,
        contextBesteschuleUserId: Int?
    ): Flow<Response<List<BesteSchuleSubject>>>

    /**
     * Prefetch subjects by IDs. Used internally for relationship preloading.
     * @param includes Optional nested PrefetchInstructions for prefetching related entities
     * @return PrefetchResult with success count and any errors
     */
    suspend fun prefetchByIds(ids: List<Int>, includes: Map<String, PrefetchInstruction> = emptyMap()): PrefetchResult
}
