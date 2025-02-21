package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.SchoolRepository

class SchoolSource(
    private val schoolRepository: SchoolRepository
) {
    private val cache = hashMapOf<Int, Flow<CacheState<School>>>()
    fun getById(
        id: Int,
    ): Flow<CacheState<School>> {
        return cache.getOrPut(id) { schoolRepository.getById(id, false) }
    }
}