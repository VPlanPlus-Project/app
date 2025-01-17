package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.LessonTime
import plus.vplan.app.domain.repository.LessonTimeRepository

class LessonTimeSource(
    private val lessonTimeRepository: LessonTimeRepository
) {
    private val cache = hashMapOf<String, Flow<CacheState<LessonTime>>>()
    private val cacheItems = hashMapOf<String, CacheState<LessonTime>>()
    fun getById(id: String): Flow<CacheState<LessonTime>> {
        return cache.getOrPut(id) {
            lessonTimeRepository.getById(id).map { if (it == null) return@map CacheState.NotExisting(id) else CacheState.Done(it) }
        }
    }

    suspend fun getSingleById(id: String): LessonTime {
        return (cacheItems[id] as? CacheState.Done<LessonTime>)?.data ?: getById(id).getFirstValue()
    }
}