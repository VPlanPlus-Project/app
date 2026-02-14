package plus.vplan.app.domain.repository.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.besteschule.BesteSchuleSubject

interface BesteSchuleSubjectsRepository {
    suspend fun addSubjectsToCache(subjects: Set<BesteSchuleSubject>)
    fun getSubjectFromCache(subjectId: Int): Flow<BesteSchuleSubject?>
}
