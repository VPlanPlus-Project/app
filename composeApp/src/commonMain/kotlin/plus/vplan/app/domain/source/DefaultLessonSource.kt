package plus.vplan.app.domain.source

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.repository.DefaultLessonRepository

class DefaultLessonSource(
    private val defaultLessonRepository: DefaultLessonRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheState<DefaultLesson>>>()
    private val cacheItems = hashMapOf<Int, CacheState<DefaultLesson>>()

    fun getById(id: Int): Flow<CacheState<DefaultLesson>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<DefaultLesson>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            MainScope().launch {
                defaultLessonRepository.getById(id).collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }

    suspend fun getSingleById(id: Int): DefaultLesson? {
        return (cacheItems[id] as? CacheState.Done<DefaultLesson>)?.data ?: getById(id).getFirstValue()
    }
}