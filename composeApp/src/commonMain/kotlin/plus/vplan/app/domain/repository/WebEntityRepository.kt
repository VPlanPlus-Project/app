package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.cache.Item

interface WebEntityRepository<T : Item<*>> {
    fun getById(id: Int, forceReload: Boolean): Flow<CacheStateOld<T>>
    fun getAllIds(): Flow<List<Int>>
}