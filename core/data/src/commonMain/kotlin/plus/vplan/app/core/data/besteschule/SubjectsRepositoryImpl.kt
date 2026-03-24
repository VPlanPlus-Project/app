package plus.vplan.app.core.data.besteschule

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import plus.vplan.app.core.database.dao.besteschule.BesteschuleSubjectDao
import plus.vplan.app.core.model.besteschule.BesteSchuleSubject

class SubjectsRepositoryImpl(
    private val subjectDao: BesteschuleSubjectDao
): SubjectsRepository {
    override fun getById(id: Int): Flow<BesteSchuleSubject?> {
        return subjectDao.getById(id)
            .map { it?.toModel() }
            .flowOn(Dispatchers.Default)
    }

    override fun getAll(): Flow<List<BesteSchuleSubject>> {
        return subjectDao.getAll()
            .map { it.map { item -> item.toModel() } }
            .flowOn(Dispatchers.Default)
    }
}