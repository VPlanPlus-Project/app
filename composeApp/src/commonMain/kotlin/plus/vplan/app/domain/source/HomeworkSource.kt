package plus.vplan.app.domain.source

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMap
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMapFactory
import plus.vplan.app.domain.repository.HomeworkRepository

class HomeworkSource : KoinComponent {
    private val homeworkRepository: HomeworkRepository by inject()
    private val concurrentHashMapFactory: ConcurrentHashMapFactory by inject()

    private val flows: ConcurrentHashMap<Int, StateFlow<CacheState<Homework>>> = concurrentHashMapFactory.create()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getById(id: Int, forceUpdate: Boolean = false): StateFlow<CacheState<Homework>> {
        if (forceUpdate) flows.remove(id)
        return flows.getOrPut(id) {
            homeworkRepository.getById(id, forceUpdate)
                .stateIn(
                    scope = scope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = CacheState.Loading(id.toString())
                )
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

class HomeworkTaskSource : KoinComponent {
    private val homeworkRepository: HomeworkRepository by inject()
    private val concurrentHashMapFactory: ConcurrentHashMapFactory by inject()

    private val cache: ConcurrentHashMap<Int, StateFlow<CacheState<Homework.HomeworkTask>>> = concurrentHashMapFactory.create()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getById(id: Int): StateFlow<CacheState<Homework.HomeworkTask>> {
        return cache.getOrPut(id) {
            homeworkRepository.getTaskById(id)
                .stateIn(
                    scope = scope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = CacheState.Loading(id.toString())
                )
        }
    }

    suspend fun getSingleById(id: Int): Homework.HomeworkTask? {
        return (cache[id]?.value as? CacheState.Done<Homework.HomeworkTask>)?.data ?: getById(id).getFirstValueOld()
    }
}