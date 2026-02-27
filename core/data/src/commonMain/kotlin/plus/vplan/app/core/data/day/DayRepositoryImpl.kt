package plus.vplan.app.core.data.day

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.database.dao.DayDao
import plus.vplan.app.core.database.model.database.DbDay
import plus.vplan.app.core.model.Day
import plus.vplan.app.core.model.School
import kotlin.uuid.Uuid

class DayRepositoryImpl(
    private val dayDao: DayDao,
) : DayRepository {
    override suspend fun save(day: Day) {
        dayDao.upsert(
            DbDay(
                id = day.id,
                date = day.date,
                info = day.info,
                weekId = day.week?.id,
                schoolId = day.school.id
            )
        )
    }

    override fun getById(id: String): Flow<Day?> {
        return dayDao
            .getBySchool(
                date = LocalDate.parse(id.substringAfter("/")),
                schoolId = Uuid.parseHex(id.substringBefore("/"))
            )
            .map { it?.toModel() }
    }

    override fun getBySchool(school: School.AppSchool): Flow<Set<Day>> {
        return dayDao
            .getBySchool(school.id)
            .map { it.map { day -> day.toModel() }.toSet() }
    }

    override fun getBySchool(school: School.AppSchool, date: LocalDate): Flow<Day?> {
        return dayDao
            .getBySchool(date, school.id)
            .map { it?.toModel() }
    }
}