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
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMap
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMapFactory
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import kotlin.uuid.Uuid

class SubjectInstanceSource : KoinComponent {
    private val subjectInstanceRepository: SubjectInstanceRepository by inject()
    private val concurrentHashMapFactory: ConcurrentHashMapFactory by inject()

    private val flows: ConcurrentHashMap<Uuid, StateFlow<AliasState<SubjectInstance>>> = concurrentHashMapFactory.create()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getById(id: Uuid): StateFlow<AliasState<SubjectInstance>> {
        return flows.getOrPut(id) {
            subjectInstanceRepository.getByLocalId(id)
                .map { it?.let { AliasState.Done(it) } ?: AliasState.NotExisting(id.toHexString()) }
                .stateIn(
                    scope = scope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = AliasState.Loading(id.toHexString())
                )
        }
    }
}
