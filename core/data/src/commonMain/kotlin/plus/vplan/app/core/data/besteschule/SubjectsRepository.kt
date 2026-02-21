package plus.vplan.app.core.data.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.besteschule.BesteSchuleSubject

interface SubjectsRepository {
    fun getById(id: Int): Flow<BesteSchuleSubject?>
    fun getAll(): Flow<List<BesteSchuleSubject>>
}