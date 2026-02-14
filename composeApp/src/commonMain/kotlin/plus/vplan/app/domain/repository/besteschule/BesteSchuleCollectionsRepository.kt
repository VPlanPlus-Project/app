package plus.vplan.app.domain.repository.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.besteschule.BesteSchuleCollection

interface BesteSchuleCollectionsRepository {
    suspend fun addCollectionsToCache(items: Set<BesteSchuleCollection>)
    fun getFromCache(collectionId: Int): Flow<BesteSchuleCollection?>
}