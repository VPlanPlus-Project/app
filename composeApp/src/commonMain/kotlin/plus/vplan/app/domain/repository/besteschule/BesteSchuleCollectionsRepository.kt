package plus.vplan.app.domain.repository.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.besteschule.BesteSchuleCollection
import plus.vplan.app.domain.model.besteschule.api.ApiStudentGradesData
import plus.vplan.app.domain.repository.base.ResponsePreference

interface BesteSchuleCollectionsRepository {
    suspend fun getCollectionsFromApi(schulverwalterAccessToken: String): Response<List<ApiStudentGradesData.Collection>>
    suspend fun addCollectionsToCache(items: Set<BesteSchuleCollection>)
    fun getFromCache(collectionId: Int): Flow<BesteSchuleCollection?>

    /**
     * @param contextBesteschuleAccessToken If a network request is being made, this is required to get the collections from the API.
     */
    fun getCollections(
        responsePreference: ResponsePreference = ResponsePreference.Fast,
        contextBesteschuleAccessToken: String? = null,
    ): Flow<Response<List<BesteSchuleCollection>>>
}