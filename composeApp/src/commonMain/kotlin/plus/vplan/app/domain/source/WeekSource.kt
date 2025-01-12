package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.repository.WeekRepository

class WeekSource(
    private val weekRepository: WeekRepository
) {
    private val cache = hashMapOf<String, Flow<CacheState<Week>>>()
    fun getById(id: String): Flow<CacheState<Week>> {
        return cache.getOrPut(id) { weekRepository.getById(id).map { if (it == null) CacheState.NotExisting(id) else CacheState.Done(it) } }
    }
}