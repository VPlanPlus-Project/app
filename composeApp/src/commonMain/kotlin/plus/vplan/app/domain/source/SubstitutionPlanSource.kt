package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMap
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMapFactory
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import kotlin.uuid.Uuid

class SubstitutionPlanSource : KoinComponent {
    private val repository: SubstitutionPlanRepository by inject()
    private val concurrentHashMapFactory: ConcurrentHashMapFactory by inject()

    private val flows: ConcurrentHashMap<Uuid, StateFlow<CacheState<Lesson.SubstitutionPlanLesson>>> = concurrentHashMapFactory.create()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getById(id: Uuid): StateFlow<CacheState<Lesson.SubstitutionPlanLesson>> {
        return flows.getOrPut(id) {
            repository.getById(id)
                .map { if (it == null) CacheState.NotExisting(id.toHexString()) else CacheState.Done(it) }
                .stateIn(
                    scope = scope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = CacheState.Loading(id.toHexString())
                )
        }
    }
}