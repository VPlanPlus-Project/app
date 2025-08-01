package plus.vplan.app.domain.source

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.repository.HomeworkRepository

class HomeworkSource(
    private val homeworkRepository: HomeworkRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheStateOld<Homework>>>()
    fun getById(id: Int, forceUpdate: Boolean = false): Flow<CacheStateOld<Homework>> {
        if (forceUpdate) flows.remove(id)
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheStateOld<Homework>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                homeworkRepository.getById(id, forceUpdate).collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAll(updateRemote: Boolean = false): Flow<List<CacheStateOld<Homework>>> {
        if (updateRemote) TODO("Not yet implemented")
        return homeworkRepository.getAll().map { it.map { it.entityId.toInt() } }.flatMapLatest { ids ->
            if (ids.isEmpty()) flowOf(emptyList())
            else combine(ids.map { getById(it) }) { it.toList() }
        }
    }
}

class HomeworkTaskSource(
    private val homeworkRepository: HomeworkRepository
) {
    private val cache = hashMapOf<Int, Flow<CacheStateOld<Homework.HomeworkTask>>>()
    private val cacheItems = hashMapOf<Int, CacheStateOld<Homework.HomeworkTask>>()

    fun getById(id: Int): Flow<CacheStateOld<Homework.HomeworkTask>> {
        return cache.getOrPut(id) { homeworkRepository.getTaskById(id).onEach { cacheItems[id] = it } }
    }

    suspend fun getSingleById(id: Int): Homework.HomeworkTask? {
        return (cacheItems[id] as? CacheStateOld.Done<Homework.HomeworkTask>)?.data ?: getById(id).getFirstValueOld()
    }
}