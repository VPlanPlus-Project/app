package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.repository.DefaultLessonRepository

class DefaultLessonSource(
    private val defaultLessonRepository: DefaultLessonRepository
) {
    private val cache = hashMapOf<String, Flow<CacheState<DefaultLesson>>>()
    fun getById(id: String): Flow<CacheState<DefaultLesson>> {
        return cache.getOrPut(id) { defaultLessonRepository.getById(id) }
    }
}