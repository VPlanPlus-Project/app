package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.AliasState
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.SchoolRepository
import kotlin.uuid.Uuid

class SchoolSource(
    private val schoolRepository: SchoolRepository
) {
    private val flows = hashMapOf<Uuid, MutableSharedFlow<AliasState<School>>>()
    private val appFlows = hashMapOf<Uuid, MutableSharedFlow<AliasState<School.AppSchool>>>()
    fun getById(
        id: Uuid,
    ): Flow<AliasState<School>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<AliasState<School>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                schoolRepository.getByLocalId(id).collectLatest { flow.tryEmit(it?.let { AliasState.Done(it) } ?: AliasState.NotExisting(id.toHexString())) }
            }
            flow
        }
    }

    fun getAppSchoolById(
        id: Uuid
    ): Flow<AliasState<School.AppSchool>> {
        return appFlows.getOrPut(id) {
            val flow = MutableSharedFlow<AliasState<School.AppSchool>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                schoolRepository.getByLocalId(id).map { it as? School.AppSchool }.collectLatest { flow.tryEmit(it?.let { AliasState.Done(it) } ?: AliasState.NotExisting(id.toHexString())) }
            }
            flow
        }
    }
}