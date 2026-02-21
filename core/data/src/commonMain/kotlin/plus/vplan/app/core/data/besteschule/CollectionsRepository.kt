package plus.vplan.app.core.data.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.besteschule.BesteSchuleCollection

interface CollectionsRepository {
    fun getById(id: Int, forceRefresh: Boolean = false): Flow<BesteSchuleCollection>
    fun getAll(forceRefresh: Boolean = false): Flow<List<BesteSchuleCollection>>
}