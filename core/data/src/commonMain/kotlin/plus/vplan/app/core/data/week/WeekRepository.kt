package plus.vplan.app.core.data.week

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.Week

interface WeekRepository {
    suspend fun save(week: Week) = save(listOf(week))
    suspend fun save(weeks: List<Week>)

    /**
     * Returns weeks for the current school year. The filtering is being handled by the
     * Repository implementation.
     */
    fun getBySchool(school: School): Flow<List<Week>>
    fun getById(id: String): Flow<Week?>

    suspend fun delete(week: Week) = delete(listOf(week))
    suspend fun delete(weeks: List<Week>)
}
