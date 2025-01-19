package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.repository.GroupRepository

class GroupSource(
    private val groupRepository: GroupRepository
) {
    private val cache = hashMapOf<Int, Flow<CacheState<Group>>>()
    private val cacheItems = hashMapOf<Int, CacheState<Group>>()

    fun getById(id: Int): Flow<CacheState<Group>> {
        return cache.getOrPut(id) { groupRepository.getById(id) }
    }

    suspend fun getSingleById(id: Int): Group? {
        return (cacheItems[id] as? CacheState.Done<Group>)?.data ?: getById(id).getFirstValue()
    }
}