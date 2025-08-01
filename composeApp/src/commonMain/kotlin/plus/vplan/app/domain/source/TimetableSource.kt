@file:OptIn(ExperimentalUuidApi::class)

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
import plus.vplan.app.domain.repository.TimetableRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TimetableSource(
    private val timetableRepository: TimetableRepository
) {
    private val flows = hashMapOf<Uuid, MutableSharedFlow<CacheStateOld<Lesson.TimetableLesson>>>()

    fun getById(id: Uuid): Flow<CacheStateOld<Lesson.TimetableLesson>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheStateOld<Lesson.TimetableLesson>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                timetableRepository.getById(id).map { if (it == null) CacheStateOld.NotExisting(id.toHexString()) else CacheStateOld.Done(it) }
                    .collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }
}