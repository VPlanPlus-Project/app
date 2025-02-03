package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.repository.DefaultLessonRepository

class DefaultLessonSource(
    private val defaultLessonRepository: DefaultLessonRepository
) {
    private val cache = hashMapOf<Int, Flow<CacheState<DefaultLesson>>>()
    private val cacheItems = hashMapOf<Int, CacheState<DefaultLesson>>()

    fun getById(id: Int): Flow<CacheState<DefaultLesson>> {
        return cache.getOrPut(id) { defaultLessonRepository.getById(id) }
    }

    suspend fun getSingleById(id: Int): DefaultLesson? {
        return (cacheItems[id] as? CacheState.Done<DefaultLesson>)?.data ?: getById(id).getFirstValue()
    }
}