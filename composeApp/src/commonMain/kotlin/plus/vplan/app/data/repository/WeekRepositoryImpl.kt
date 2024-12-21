package plus.vplan.app.data.repository

import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbWeek
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.repository.WeekRepository

class WeekRepositoryImpl(
    private val vppDatabase: VppDatabase
) : WeekRepository {
    override suspend fun insert(week: Week) {
        vppDatabase.weekDao.upsert(
            DbWeek(
                id = week.id,
                schoolId = week.school.id,
                calendarWeek = week.calendarWeek,
                start = week.start,
                end = week.end,
                weekType = week.weekType,
                weekIndex = week.weekIndex
            )
        )
    }

}