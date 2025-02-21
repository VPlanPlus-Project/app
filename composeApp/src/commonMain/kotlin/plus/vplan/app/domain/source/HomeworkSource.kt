package plus.vplan.app.domain.source

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.repository.HomeworkRepository

class HomeworkSource(
    private val homeworkRepository: HomeworkRepository
) {
    private val cache = hashMapOf<Int, Flow<CacheState<Homework>>>()
    fun getById(id: Int): Flow<CacheState<Homework>> {
        return cache.getOrPut(id) { homeworkRepository.getById(id, false) }
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
    private val cache = hashMapOf<Int, Flow<CacheState<Homework.HomeworkTask>>>()
    private val cacheItems = hashMapOf<Int, CacheState<Homework.HomeworkTask>>()

    fun getById(id: Int): Flow<CacheState<Homework.HomeworkTask>> {
        return cache.getOrPut(id) { homeworkRepository.getTaskById(id).onEach { cacheItems[id] = it } }
    }

    suspend fun getSingleById(id: Int): Homework.HomeworkTask? {
        return (cacheItems[id] as? CacheState.Done<Homework.HomeworkTask>)?.data ?: getById(id).getFirstValue()
    }
}