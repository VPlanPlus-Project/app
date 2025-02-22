package plus.vplan.app.domain.source

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.LessonTime
import plus.vplan.app.domain.repository.LessonTimeRepository

class LessonTimeSource(
    private val lessonTimeRepository: LessonTimeRepository
) {
    private val flows = hashMapOf<String, MutableSharedFlow<CacheState<LessonTime>>>()
    private val cacheItems = hashMapOf<String, CacheState<LessonTime>>()
    fun getById(id: String): Flow<CacheState<LessonTime>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<LessonTime>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            MainScope().launch {
                lessonTimeRepository.getById(id).map { if (it == null) return@map CacheState.NotExisting(id) else CacheState.Done(it) }
                    .collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }

    suspend fun getSingleById(id: String): LessonTime? {
        return (cacheItems[id] as? CacheState.Done<LessonTime>)?.data ?: getById(id).getFirstValue()
    }
}