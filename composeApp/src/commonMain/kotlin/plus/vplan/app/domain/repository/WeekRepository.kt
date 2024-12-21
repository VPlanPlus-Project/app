package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.Week

interface WeekRepository {
    suspend fun upsert(week: Week)

    fun getBySchool(schoolId: Int): Flow<List<Week>>

    suspend fun deleteBySchool(schoolId: Int)
    suspend fun deleteById(id: String)
}