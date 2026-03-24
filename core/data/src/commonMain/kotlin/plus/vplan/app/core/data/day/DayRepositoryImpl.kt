package plus.vplan.app.core.data.day

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.database.dao.DayDao
import plus.vplan.app.core.database.model.database.DbDay
import plus.vplan.app.core.model.Day
import plus.vplan.app.core.model.School
import kotlin.uuid.Uuid

class DayRepositoryImpl(
    private val dayDao: DayDao,
    private val applicationScope: CoroutineScope,
) : DayRepository {

    private val bySchoolCache = mutableMapOf<Uuid, Flow<Set<Day>>>()

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
                schoolId = Uuid.parse(id.substringBefore("/"))
            )
            .map { it?.toModel() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    override fun getBySchool(school: School.AppSchool): Flow<Set<Day>> {
        return bySchoolCache.getOrPut(school.id) {
            dayDao
                .getBySchool(school.id)
                .map { it.map { day -> day.toModel() }.toSet() }
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .shareIn(applicationScope, SharingStarted.WhileSubscribed(5_000L), replay = 1)
        }
    }

    override fun getBySchool(school: School.AppSchool, date: LocalDate): Flow<Day?> {
        return dayDao
            .getBySchool(date, school.id)
            .map { it?.toModel() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }
}