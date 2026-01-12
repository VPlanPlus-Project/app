package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.AliasState
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.data_structure.ConcurrentMutableMap
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import kotlin.uuid.Uuid

class SubjectInstanceSource(
    private val subjectInstanceRepository: SubjectInstanceRepository
) {
    private val flows: ConcurrentMutableMap<Uuid, MutableSharedFlow<AliasState<SubjectInstance>>> = ConcurrentMutableMap()

    fun getById(id: Uuid): Flow<AliasState<SubjectInstance>> {
        return channelFlow {
            flows.getOrPut(id) {
                val flow = MutableSharedFlow<AliasState<SubjectInstance>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
                CoroutineScope(Dispatchers.IO).launch {
                    subjectInstanceRepository.getByLocalId(id).collectLatest { flow.tryEmit(it?.let { AliasState.Done(it) } ?: AliasState.NotExisting(id.toHexString())) }
                }
                return@getOrPut flow
            }.collect { send(it) }
        }
    }
}
