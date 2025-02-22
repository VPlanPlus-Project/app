package plus.vplan.app.domain.source

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.repository.TimetableRepository
import kotlin.uuid.Uuid

class TimetableSource(
    private val timetableRepository: TimetableRepository
) {
    private val flows = hashMapOf<Uuid, MutableSharedFlow<CacheState<Lesson.TimetableLesson>>>()
    fun getBySchool(schoolId: Int): Flow<List<CacheState<Lesson>>> = channelFlow {
        timetableRepository.getTimetableForSchool(schoolId).map { it.map { lesson -> lesson.id } }.collectLatest { timetableLessonIds ->
            combine(timetableLessonIds.map { getById(it) }) { it }.collectLatest { send(it.toList()) }
        }
    }

    fun getById(id: Uuid): Flow<CacheState<Lesson.TimetableLesson>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<Lesson.TimetableLesson>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            MainScope().launch {
                timetableRepository.getById(id).map { if (it == null) CacheState.NotExisting(id.toHexString()) else CacheState.Done(it) }
                    .collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }
}