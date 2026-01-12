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
import plus.vplan.app.domain.model.Teacher
import plus.vplan.app.domain.model.data_structure.ConcurrentMutableMap
import plus.vplan.app.domain.repository.TeacherRepository
import kotlin.uuid.Uuid

class TeacherSource(
    private val teacherRepository: TeacherRepository
) {

    private val flows: ConcurrentMutableMap<Uuid, MutableSharedFlow<AliasState<Teacher>>> = ConcurrentMutableMap()
    fun getById(id: Uuid, forceReload: Boolean = false): Flow<AliasState<Teacher>> {
        if (forceReload) kotlinx.coroutines.runBlocking { flows.remove(id) }
        return channelFlow {
            flows.getOrPut(id) {
                val flow = MutableSharedFlow<AliasState<Teacher>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
                CoroutineScope(Dispatchers.IO).launch {
                    teacherRepository.getByLocalId(id).collectLatest { flow.tryEmit(it?.let { AliasState.Done(it) } ?: AliasState.NotExisting(id.toHexString())) }
                }
                return@getOrPut flow
            }.collect { send(it) }
        }
    }
}