package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Item

interface WebEntityRepository<T : Item<*, *>> {
    fun getById(id: Int, forceReload: Boolean): Flow<CacheState<T>>
    fun getAllIds(): Flow<List<Int>>
}