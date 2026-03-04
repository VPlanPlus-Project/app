package plus.vplan.app.core.data.holiday

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.core.database.dao.HolidayDao
import plus.vplan.app.core.database.model.database.DbHoliday
import plus.vplan.app.core.model.Holiday
import plus.vplan.app.core.model.School

class HolidayRepositoryImpl(
    private val holidayDao: HolidayDao
) : HolidayRepository {
    override fun getAll(): Flow<List<Holiday>> {
        return holidayDao.getAll().map { items -> items.map { it.toModel() } }
    }

    override fun getBySchool(school: School.AppSchool): Flow<List<Holiday>> {
        return holidayDao.getBySchoolId(school.id).map { items -> items.map { it.toModel() } }
    }

    override suspend fun save(holidays: List<Holiday>) {
        return holidayDao.upsert(holidays.map {
            DbHoliday(
                id = it.id,
                date = it.date,
                schoolId = it.school
            )
        })
    }

    override suspend fun delete(holidays: List<Holiday>) {
        return holidayDao.deleteByIds(holidays.map { it.id })
    }
}