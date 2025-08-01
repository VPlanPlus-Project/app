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
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.LessonTime
import plus.vplan.app.domain.repository.LessonTimeRepository

class LessonTimeSource(
    private val lessonTimeRepository: LessonTimeRepository
) {
    private val flows = hashMapOf<String, MutableSharedFlow<CacheStateOld<LessonTime>>>()
    private val cacheItems = hashMapOf<String, CacheStateOld<LessonTime>>()
    fun getById(id: String): Flow<CacheStateOld<LessonTime>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheStateOld<LessonTime>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                lessonTimeRepository.getById(id).map { if (it == null) return@map CacheStateOld.NotExisting(id) else CacheStateOld.Done(it) }
                    .collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }

    suspend fun getSingleById(id: String): LessonTime? {
        return (cacheItems[id] as? CacheStateOld.Done<LessonTime>)?.data ?: getById(id).getFirstValueOld()
    }
}