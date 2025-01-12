package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.repository.TimetableRepository
import kotlin.uuid.Uuid

class TimetableSource(
    private val timetableRepository: TimetableRepository
) {
    private val cache = hashMapOf<Uuid, Flow<CacheState<Lesson.TimetableLesson>>>()
    fun getBySchool(schoolId: Int): Flow<List<CacheState<Lesson>>> = channelFlow {
        timetableRepository.getTimetableForSchool(schoolId).map { it.map { lesson -> lesson.id } }.collectLatest { timetableLessonIds ->
            combine(timetableLessonIds.map { getById(it) }) { it }.collectLatest { send(it.toList()) }
        }
    }

    fun getById(id: Uuid): Flow<CacheState<Lesson>> {
        return cache.getOrPut(id) {
            timetableRepository.getById(id).map { if (it == null) CacheState.NotExisting(id.toHexString()) else CacheState.Done(it) }
        }
    }
}