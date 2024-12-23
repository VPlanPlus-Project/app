package plus.vplan.app.domain.repository

import plus.vplan.app.domain.model.Day

interface DayRepository {

    suspend fun insert(day: Day)
}