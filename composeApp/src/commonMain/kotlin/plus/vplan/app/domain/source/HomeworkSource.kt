package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.getFirstValueOld
import plus.vplan.app.core.model.Homework
import plus.vplan.app.domain.repository.HomeworkRepository

class HomeworkSource(
    private val homeworkRepository: HomeworkRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheState<Homework>>>()
    fun getById(id: Int, forceUpdate: Boolean = false): Flow<CacheState<Homework>> {
        if (forceUpdate) flows.remove(id)
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<Homework>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                homeworkRepository.getById(id, forceUpdate).collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAll(updateRemote: Boolean = false): Flow<List<Homework>> {
        if (updateRemote) TODO("Not yet implemented")
        return homeworkRepository.getAll()
    }
}

class HomeworkTaskSource(
    private val homeworkRepository: HomeworkRepository
) {
    private val cache = hashMapOf<Int, Flow<CacheState<Homework.HomeworkTask>>>()
    private val cacheItems = hashMapOf<Int, CacheState<Homework.HomeworkTask>>()

    fun getById(id: Int): Flow<CacheState<Homework.HomeworkTask>> {
        return cache.getOrPut(id) { homeworkRepository.getTaskById(id).onEach { cacheItems[id] = it } }
    }

    suspend fun getSingleById(id: Int): Homework.HomeworkTask? {
        return (cacheItems[id] as? CacheState.Done<Homework.HomeworkTask>)?.data ?: getById(id).getFirstValueOld()
    }
}