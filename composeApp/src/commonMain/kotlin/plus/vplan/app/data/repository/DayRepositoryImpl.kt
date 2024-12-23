package plus.vplan.app.data.repository

import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbDay
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.repository.DayRepository

class DayRepositoryImpl(
    private val vppDatabase: VppDatabase
) : DayRepository {
    override suspend fun insert(day: Day) {
        vppDatabase.dayDao.upsert(DbDay(
            id = day.id,
            date = day.date,
            info = day.info,
            weekId = day.week.id,
            schoolId = day.school.id
        ))
    }
}