package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.domain.cache.AliasState
import plus.vplan.app.domain.model.Room
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMap
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMapFactory
import plus.vplan.app.domain.repository.RoomRepository
import kotlin.uuid.Uuid

class RoomSource : KoinComponent {
    private val roomRepository: RoomRepository by inject()
    private val concurrentHashMapFactory: ConcurrentHashMapFactory by inject()

    private val flows: ConcurrentHashMap<Uuid, StateFlow<AliasState<Room>>> = concurrentHashMapFactory.create()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getById(id: Uuid, forceReload: Boolean = false): StateFlow<AliasState<Room>> {
        if (forceReload) flows.remove(id)
        return flows.getOrPut(id) {
            roomRepository.getByLocalId(id)
                .map { it?.let { AliasState.Done(it) } ?: AliasState.NotExisting(id.toHexString()) }
                .stateIn(
                    scope = scope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = AliasState.Loading(id.toHexString())
                )
        }
    }
}