package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.repository.HomeworkRepository

class HomeworkSource(
    private val homeworkRepository: HomeworkRepository
) {
    private val cache = hashMapOf<Int, Flow<CacheState<Homework>>>()
    fun getById(id: Int): Flow<CacheState<Homework>> {
        return cache.getOrPut(id) { homeworkRepository.getById(id) }
    }
}