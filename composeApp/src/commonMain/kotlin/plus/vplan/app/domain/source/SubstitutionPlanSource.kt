package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import kotlin.uuid.Uuid

class SubstitutionPlanSource(
    private val repository: SubstitutionPlanRepository
) {
    private val cache = hashMapOf<Uuid, Flow<CacheState<Lesson.SubstitutionPlanLesson>>>()
    fun getById(id: Uuid): Flow<CacheState<Lesson.SubstitutionPlanLesson>> {
        return cache.getOrPut(id) {
            repository.getById(id).map {
                if (it == null) CacheState.NotExisting(id.toHexString()) else CacheState.Done(it)
            }
        }
    }
}