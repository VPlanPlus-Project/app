package plus.vplan.app.domain.source

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.repository.GroupRepository

class GroupSource(
    private val groupRepository: GroupRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheState<Group>>>()
    private val cacheItems = hashMapOf<Int, CacheState<Group>>()

    fun getById(id: Int, forceReload: Boolean = false): Flow<CacheState<Group>> {
        if (forceReload) flows.remove(id)
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<Group>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            MainScope().launch {
                groupRepository.getById(id, forceReload).collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }

    suspend fun getSingleById(id: Int): Group? {
        return (cacheItems[id] as? CacheState.Done<Group>)?.data ?: getById(id).getFirstValue()
    }
}