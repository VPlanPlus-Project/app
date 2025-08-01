package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.Week
import kotlin.uuid.Uuid

interface WeekRepository {
    suspend fun upsert(week: Week)
    suspend fun upsert(weeks: List<Week>)

    fun getBySchool(schoolId: Uuid): Flow<List<Week>>
    fun getById(id: String): Flow<Week?>

    suspend fun deleteBySchool(schoolId: Uuid)
    suspend fun deleteById(id: String)
    suspend fun deleteById(id: List<String>)
}