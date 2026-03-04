package plus.vplan.app.core.data.day

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.model.Day
import plus.vplan.app.core.model.School

interface DayRepository {
    suspend fun save(day: Day)
    fun getById(id: String): Flow<Day?>
    fun getBySchool(school: School.AppSchool): Flow<Set<Day>>
    fun getBySchool(school: School.AppSchool, date: LocalDate): Flow<Day?>
}