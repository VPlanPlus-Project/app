package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.utils.minus
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

class DaySource(
    private val dayRepository: DayRepository,
    private val weekRepository: WeekRepository,
    private val timetableRepository: TimetableRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository
) {
    private val cache = hashMapOf<String, Flow<CacheState<Day>>>()
    fun getById(id: String): Flow<CacheState<Day>> {
        return cache.getOrPut(id) {
            channelFlow {
                val schoolId = id.substringBefore("/").toInt()
                val date = LocalDate.parse(id.substringAfter("/"))
                val school = App.schoolSource.getById(schoolId).filterIsInstance<CacheState.Done<School>>().first().data
                combine(
                    weekRepository.getBySchool(schoolId),
                    dayRepository.getHolidays(schoolId).map { it.map { holiday -> holiday.date } },
                    dayRepository.getBySchool(date, schoolId)
                ) { weeks, holidays, dayInfo ->
                    val dayWeek = weeks.firstOrNull { date in it.start..it.end } ?: weeks.last { it.start < date }
                    MetaEmitting(
                        dayWeek = dayWeek,
                        dayInfo = dayInfo,
                        holidays = holidays
                    )
                }.collectLatest { meta ->
                    val findNextRegularSchoolDayAfter: (LocalDate) -> LocalDate? = findNextRegularSchoolDayAfter@{ startDate ->
                        if (meta.holidays.isEmpty()) return@findNextRegularSchoolDayAfter null
                        var nextSchoolDay = startDate + 1.days
                        while (meta.holidays.maxOf { it } > nextSchoolDay && !(meta.holidays.none { it == nextSchoolDay } && nextSchoolDay.dayOfWeek.isoDayNumber < ((school as? School.IndiwareSchool)?.daysPerWeek ?: 5))) {
                            nextSchoolDay += 1.days
                        }
                        nextSchoolDay
                    }
                    combine(
                        timetableRepository.getForSchool(
                            schoolId = schoolId,
                            dayOfWeek = date.dayOfWeek,
                            weekIndex = meta.dayWeek.weekIndex,
                        ),
                        substitutionPlanRepository.getSubstitutionPlanBySchool(schoolId, date)
                    ) { timetable, substitutionPlan ->
                        send(CacheState.Done(Day(
                            id = id,
                            date = date,
                            school = schoolId,
                            week = meta.dayWeek.id,
                            info = meta.dayInfo?.info,
                            dayType =
                            if (date in meta.holidays) Day.DayType.HOLIDAY
                            else if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
                                var friday = date
                                while (friday.dayOfWeek != DayOfWeek.FRIDAY) {
                                    friday -= 1.days
                                }
                                var monday = date
                                while (monday.dayOfWeek != DayOfWeek.MONDAY) {
                                    monday += 1.days
                                }
                                if (meta.holidays.any { it == friday } || meta.holidays.any { it == monday }) Day.DayType.HOLIDAY
                                else Day.DayType.WEEKEND
                            }
                            else if (timetable.isNotEmpty()) Day.DayType.REGULAR
                            else Day.DayType.UNKNOWN,
                            timetable = timetable,
                            substitutionPlan = substitutionPlan,
                            nextSchoolDay = findNextRegularSchoolDayAfter(date)?.let { "$schoolId/$it" }
                        )))
                    }.collectLatest {  }
                }
            }
        }
    }
}

private data class MetaEmitting(
    val dayWeek: Week,
    val dayInfo: Day?,
    val holidays: List<LocalDate>
)