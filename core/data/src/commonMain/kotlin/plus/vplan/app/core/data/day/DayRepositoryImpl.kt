@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.core.data.day

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import plus.vplan.app.core.data.week.WeekRepository
import plus.vplan.app.core.database.dao.DayDao
import plus.vplan.app.core.database.model.database.DbDay
import plus.vplan.app.core.model.Day
import plus.vplan.app.core.model.School
import plus.vplan.app.core.utils.date.plus
import kotlin.time.Duration.Companion.days
import kotlin.uuid.Uuid

class DayRepositoryImpl(
    private val dayDao: DayDao,
    private val weekRepository: WeekRepository,
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
    }

    override fun getBySchool(school: School.AppSchool): Flow<Set<Day>> {
        return bySchoolCache.getOrPut(school.id) {
            dayDao
                .getBySchool(school.id)
                .map { it.map { day -> day.toModel() }.toSet() }
                .distinctUntilChanged()
                .shareIn(applicationScope, SharingStarted.WhileSubscribed(5_000L), replay = 1)
        }
    }

    override fun getBySchool(school: School.AppSchool, date: LocalDate): Flow<Day> {
        return dayDao
            .getBySchool(date, school.id)
            .flatMapLatest { dayDao ->
                if (dayDao != null) flowOf(dayDao.toModel())
                else weekRepository.getBySchool(school).map { weeks ->
                    Day(
                        id = Day.buildId(school.id, date),
                        date = date,
                        school = school,
                        week = weeks.find { week -> date in week.start..week.end },
                        dayType = if (date.dayOfWeek.isoDayNumber > school.daysPerWeek) Day.DayType.WEEKEND else Day.DayType.REGULAR,
                        nextSchoolDay = date + 1.days,
                        info = null,
                    )
                }
            }
            .distinctUntilChanged()
    }
}