package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.Week

interface WeekRepository {
    suspend fun upsert(week: Week)
    suspend fun upsert(weeks: List<Week>)

    fun getBySchool(schoolId: Int): Flow<List<Week>>

    suspend fun deleteBySchool(schoolId: Int)
    suspend fun deleteById(id: String)
    suspend fun deleteById(id: List<String>)
}