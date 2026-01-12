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
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.data_structure.ConcurrentMutableMap
import plus.vplan.app.domain.repository.HomeworkRepository

class HomeworkSource(
    private val homeworkRepository: HomeworkRepository
) {
    private val flows: ConcurrentMutableMap<Int, MutableSharedFlow<CacheState<Homework>>> = ConcurrentMutableMap()
    fun getById(id: Int, forceUpdate: Boolean = false): Flow<CacheState<Homework>> {
        if (forceUpdate) kotlinx.coroutines.runBlocking { flows.remove(id) }
        return channelFlow {
            flows.getOrPut(id) {
                val flow = MutableSharedFlow<CacheState<Homework>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
                CoroutineScope(Dispatchers.IO).launch {
                    homeworkRepository.getById(id, forceUpdate).collectLatest { flow.tryEmit(it) }
                }
                return@getOrPut flow
            }.collect { send(it) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAll(updateRemote: Boolean = false): Flow<List<CacheState<Homework>>> {
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
    private val cache: ConcurrentMutableMap<Int, Flow<CacheState<Homework.HomeworkTask>>> = ConcurrentMutableMap()
    private val cacheItems: ConcurrentMutableMap<Int, CacheState<Homework.HomeworkTask>> = ConcurrentMutableMap()

    fun getById(id: Int): Flow<CacheState<Homework.HomeworkTask>> {
        return channelFlow {
            cache.getOrPut(id) { homeworkRepository.getTaskById(id).onEach { cacheItems[id] = it } }.collect { send(it) }
        }
    }

    suspend fun getSingleById(id: Int): Homework.HomeworkTask? {
        return (cacheItems[id] as? CacheState.Done<Homework.HomeworkTask>)?.data ?: getById(id).getFirstValueOld()
    }
}