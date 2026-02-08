package plus.vplan.app.domain.repository.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.besteschule.BesteSchuleInterval
import plus.vplan.app.domain.model.besteschule.api.ApiStudentData
import plus.vplan.app.domain.repository.base.PrefetchInstruction
import plus.vplan.app.domain.repository.base.PrefetchResult
import plus.vplan.app.domain.repository.base.PrefetchableRepository
import plus.vplan.app.domain.repository.base.ResponsePreference

interface BesteSchuleIntervalsRepository : PrefetchableRepository {
    /**
     * Uses [BesteSchuleApiRepository.getStudentData] in the background
     */
    suspend fun getIntervalsFromApi(schulverwalterAccessToken: String, withCache: Boolean): Response<List<ApiStudentData.Interval>>
    suspend fun addIntervalsToCache(intervals: Set<BesteSchuleInterval>)
    fun getIntervalFromCache(intervalId: Int): Flow<BesteSchuleInterval?>


    /**
     * @param contextBesteschuleAccessToken If a network request is being made, this is required to get the intervals from the API.
     * @param contextBesteschuleUserId If set, used as a filter.
     * @param includes Map of relationship names to PrefetchInstruction configurations for prefetching related entities
     */
    fun getIntervals(
        responsePreference: ResponsePreference = ResponsePreference.Fast,
        contextBesteschuleAccessToken: String? = null,
        contextBesteschuleUserId: Int? = null,
        withCache: Boolean = true,
        includes: Map<String, PrefetchInstruction> = emptyMap()
    ): Flow<Response<List<BesteSchuleInterval>>>

    /**
     * Prefetch intervals by IDs. Used internally for relationship preloading.
     * @param includes Optional nested PrefetchInstructions for prefetching related entities
     * @return PrefetchResult with success count and any errors
     */
    suspend fun prefetchByIds(ids: List<Int>, includes: Map<String, PrefetchInstruction> = emptyMap()): PrefetchResult
}
