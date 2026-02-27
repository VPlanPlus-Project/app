package plus.vplan.app.core.data.week

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.core.database.dao.WeekDao
import plus.vplan.app.core.database.model.database.DbWeek
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.Week

class WeekRepositoryImpl(
    private val weekDao: WeekDao,
) : WeekRepository {
    override suspend fun save(weeks: List<Week>) {
        weekDao.upsert(weeks.map { week ->
            DbWeek(
                id = week.id,
                schoolId = week.school,
                calendarWeek = week.calendarWeek,
                start = week.start,
                end = week.end,
                weekType = week.weekType,
                weekIndex = week.weekIndex
            )
        })
    }

    override fun getBySchool(school: School): Flow<List<Week>> {
        return weekDao
            .getBySchool(school.id).map { it.map { embeddedWeek -> embeddedWeek.toModel() } }
    }

    override fun getById(id: String): Flow<Week?> {
        return weekDao.getById(id).map { it?.toModel() }
    }

    override suspend fun delete(weeks: List<Week>) {
        weekDao.deleteById(weeks.map { it.id })
    }
}