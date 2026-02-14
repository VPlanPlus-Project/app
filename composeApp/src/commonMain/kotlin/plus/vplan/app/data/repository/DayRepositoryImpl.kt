package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbDay
import plus.vplan.app.data.source.database.model.database.DbHoliday
import plus.vplan.app.domain.model.Day
import plus.vplan.app.core.model.Holiday
import plus.vplan.app.domain.repository.DayRepository
import kotlin.uuid.Uuid

class DayRepositoryImpl(
    private val vppDatabase: VppDatabase
) : DayRepository {
    override suspend fun insert(day: Day) {
        vppDatabase.dayDao.upsert(DbDay(
            id = day.id,
            date = day.date,
            info = day.info,
            weekId = day.weekId,
            schoolId = day.schoolId
        ))
    }

    override suspend fun upsert(day: Holiday) {
        upsert(listOf(day))
    }

    override suspend fun upsert(holidays: List<Holiday>) {
        vppDatabase.holidayDao.upsert(holidays.map { holiday ->
            DbHoliday(
                id = holiday.id,
                date = holiday.date,
                schoolId = holiday.school
            )
        })
    }

    override suspend fun getHolidays(schoolId: Uuid): Flow<List<Holiday>> {
        return vppDatabase.holidayDao.getBySchoolId(schoolId).map { holidays ->
            holidays.map { it.toModel() }
        }
    }

    override suspend fun deleteHolidayById(id: String) {
        deleteHolidaysByIds(listOf(id))
    }

    override suspend fun deleteHolidaysByIds(ids: List<String>) {
        vppDatabase.holidayDao.deleteByIds(ids)
    }

    override fun getBySchool(date: LocalDate, schoolId: Uuid): Flow<Day?> {
        return vppDatabase.dayDao.getBySchool(date, schoolId).map { it?.toModel() }
    }

    override fun getBySchool(schoolId: Uuid): Flow<Set<Day>> {
        return vppDatabase.dayDao.getBySchool(schoolId).map { it.map { day -> day.toModel() }.toSet() }
    }

    override fun getById(id: String): Flow<Day?> {
        return getBySchool(LocalDate.parse(id.substringAfter("/")), Uuid.parseHex(id.substringBefore("/")))
    }
}