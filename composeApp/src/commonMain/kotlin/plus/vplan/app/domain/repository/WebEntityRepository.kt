package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.Item

interface WebEntityRepository<T : Item<*, *>> {
    fun getById(id: Int, forceReload: Boolean): Flow<CacheState<T>>
    fun getAllIds(): Flow<List<Int>>
}