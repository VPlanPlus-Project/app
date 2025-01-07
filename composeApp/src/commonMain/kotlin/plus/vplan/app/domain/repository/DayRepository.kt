package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Holiday

interface DayRepository {

    suspend fun insert(day: Day)
    suspend fun upsert(day: Holiday)
    suspend fun upsert(holidays: List<Holiday>)
    suspend fun getHolidays(schoolId: Int): Flow<List<Holiday>>
    suspend fun deleteHolidayById(id: String)
    suspend fun deleteHolidaysByIds(ids: List<String>)
    fun getBySchool(date: LocalDate, schoolId: Int): Flow<Day?>
    fun getById(id: String): Flow<Day?>
}