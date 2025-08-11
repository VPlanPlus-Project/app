package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.AliasState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.repository.GroupRepository
import kotlin.uuid.Uuid

class GroupSource(
    private val groupRepository: GroupRepository
) {
    private val flows = hashMapOf<Uuid, MutableSharedFlow<AliasState<Group>>>()
    private val cacheItems = hashMapOf<Uuid, AliasState<Group>>()

    fun getById(id: Uuid, forceReload: Boolean = false): Flow<AliasState<Group>> {
        if (forceReload) flows.remove(id)
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<AliasState<Group>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                groupRepository.getByLocalId(id).collectLatest { flow.tryEmit(it?.let { AliasState.Done(it) } ?: AliasState.NotExisting(id.toHexString())) }
            }
            flow
        }
    }

    suspend fun getSingleById(id: Uuid): Group? {
        return (cacheItems[id] as? AliasState.Done<Group>)?.data ?: getById(id).getFirstValue()
    }
}