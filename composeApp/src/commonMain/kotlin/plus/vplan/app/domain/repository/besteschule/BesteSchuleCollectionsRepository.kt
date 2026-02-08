package plus.vplan.app.domain.repository.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.besteschule.BesteSchuleCollection
import plus.vplan.app.domain.model.besteschule.api.ApiStudentGradesData
import plus.vplan.app.domain.repository.base.PrefetchInstruction
import plus.vplan.app.domain.repository.base.PrefetchResult
import plus.vplan.app.domain.repository.base.PrefetchableRepository
import plus.vplan.app.domain.repository.base.ResponsePreference

interface BesteSchuleCollectionsRepository : PrefetchableRepository {
    suspend fun getCollectionsFromApi(schulverwalterAccessToken: String): Response<List<ApiStudentGradesData.Collection>>
    suspend fun addCollectionsToCache(items: Set<BesteSchuleCollection>)
    fun getFromCache(collectionId: Int): Flow<BesteSchuleCollection?>

    /**
     * @param contextBesteschuleAccessToken If a network request is being made, this is required to get the collections from the API.
     * @param includes Map of relationship names to PrefetchInstruction for prefetching related entities
     */
    fun getCollections(
        responsePreference: ResponsePreference = ResponsePreference.Fast,
        contextBesteschuleAccessToken: String? = null,
        includes: Map<String, PrefetchInstruction> = emptyMap()
    ): Flow<Response<List<BesteSchuleCollection>>>

    /**
     * Prefetch collections by IDs. Used internally for relationship preloading.
     * @param includes Optional nested includes for prefetching related entities
     * @return PrefetchResult with success count and any errors
     */
    suspend fun prefetchByIds(ids: List<Int>, includes: Map<String, PrefetchInstruction> = emptyMap()): PrefetchResult
}
