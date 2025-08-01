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
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import kotlin.uuid.Uuid

class SubstitutionPlanSource(
    private val repository: SubstitutionPlanRepository
) {
    private val flow = hashMapOf<Uuid, MutableSharedFlow<CacheStateOld<Lesson.SubstitutionPlanLesson>>>()
    fun getById(id: Uuid): Flow<CacheStateOld<Lesson.SubstitutionPlanLesson>> {
        return flow.getOrPut(id) {
            val flow = MutableSharedFlow<CacheStateOld<Lesson.SubstitutionPlanLesson>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                repository.getById(id).map { if (it == null) CacheStateOld.NotExisting(id.toHexString()) else CacheStateOld.Done(it) }
                    .collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }
}