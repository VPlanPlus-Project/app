package plus.vplan.app.core.data.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.besteschule.BesteSchuleInterval

interface IntervalsRepository {
    fun getById(id: Int, forceRefresh: Boolean = false): Flow<BesteSchuleInterval>
    fun getAll(forceRefresh: Boolean = false): Flow<List<BesteSchuleInterval>>
}