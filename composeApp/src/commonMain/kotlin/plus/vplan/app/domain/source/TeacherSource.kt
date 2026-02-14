package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.core.model.AliasState
import plus.vplan.app.core.model.getFirstValue
import plus.vplan.app.core.model.Teacher
import plus.vplan.app.domain.repository.TeacherRepository
import kotlin.uuid.Uuid

class TeacherSource(
    private val teacherRepository: TeacherRepository
) {

    private val flows = hashMapOf<Uuid, MutableSharedFlow<AliasState<Teacher>>>()
    private val cacheItems = hashMapOf<Uuid, AliasState<Teacher>>()
    fun getById(id: Uuid, forceReload: Boolean = false): Flow<AliasState<Teacher>> {
        if (forceReload) flows.remove(id)
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<AliasState<Teacher>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                teacherRepository.getByLocalId(id).collectLatest { flow.tryEmit(it?.let { AliasState.Done(it) } ?: AliasState.NotExisting(id.toHexString())) }
            }
            flow
        }
    }

    suspend fun getSingleById(id: Uuid): Teacher? {
        return (cacheItems[id] as? AliasState.Done<Teacher>)?.data ?: getById(id).getFirstValue()
    }
}