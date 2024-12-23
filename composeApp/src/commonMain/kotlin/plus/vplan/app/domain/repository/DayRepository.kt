package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.Day

interface DayRepository {

    suspend fun insert(day: Day)
    fun getBySchool(date: LocalDate, schoolId: Int): Flow<Day?>
}