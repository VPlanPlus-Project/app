package plus.vplan.app.core.data.holiday

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.Holiday
import plus.vplan.app.core.model.School

interface HolidayRepository {
    fun getAll(): Flow<List<Holiday>>
    fun getBySchool(school: School.AppSchool): Flow<List<Holiday>>

    suspend fun save(holiday: Holiday) = save(listOf(holiday))
    suspend fun save(holidays: List<Holiday>)
    suspend fun delete(holiday: Holiday) = delete(listOf(holiday))
    suspend fun delete(holidays: List<Holiday>)
}