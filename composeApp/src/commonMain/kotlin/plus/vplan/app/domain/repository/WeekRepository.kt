package plus.vplan.app.domain.repository

import plus.vplan.app.domain.model.Week

interface WeekRepository {
    suspend fun insert(week: Week)
}