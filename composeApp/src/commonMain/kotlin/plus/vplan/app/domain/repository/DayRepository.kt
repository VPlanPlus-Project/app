package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.model.Day
import plus.vplan.app.core.model.Holiday
import kotlin.uuid.Uuid

interface DayRepository {

    suspend fun insert(day: Day)
    suspend fun upsert(day: Holiday)
    suspend fun upsert(holidays: List<Holiday>)
    suspend fun getHolidays(schoolId: Uuid): Flow<List<Holiday>>
    suspend fun deleteHolidayById(id: String)
    suspend fun deleteHolidaysByIds(ids: List<String>)
    fun getBySchool(schoolId: Uuid): Flow<Set<Day>>
    fun getBySchool(date: LocalDate, schoolId: Uuid): Flow<Day?>
    fun getById(id: String): Flow<Day?>
}