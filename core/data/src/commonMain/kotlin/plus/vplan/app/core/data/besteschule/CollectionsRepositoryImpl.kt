package plus.vplan.app.core.data.besteschule

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import plus.vplan.app.core.database.dao.besteschule.BesteschuleCollectionDao
import plus.vplan.app.core.model.besteschule.BesteSchuleCollection

class CollectionsRepositoryImpl(
    private val collectionDao: BesteschuleCollectionDao,
): CollectionsRepository {
    override fun getById(
        id: Int,
        forceRefresh: Boolean
    ): Flow<BesteSchuleCollection?> {
        return collectionDao.getById(id)
            .map { item -> item?.toModel() }
            .flowOn(Dispatchers.Default)
    }

    override fun getAll(forceRefresh: Boolean): Flow<List<BesteSchuleCollection>> {
        return collectionDao.getAll()
            .map { items -> items.map { it.toModel() } }
            .flowOn(Dispatchers.Default)
    }
}
