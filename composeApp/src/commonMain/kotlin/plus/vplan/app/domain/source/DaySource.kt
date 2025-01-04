package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.utils.minus
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

class DaySource(
    private val dayRepository: DayRepository,
    private val weekRepository: WeekRepository,
    private val timetableRepository: TimetableRepository
) : CacheableItemSource<Day> {
    override fun getAll(configuration: CacheableItemSource.FetchConfiguration<Day>): Flow<List<Cacheable<Day>>> {
        TODO("Not yet implemented")
    }

    override fun getById(
        id: String,
        configuration: CacheableItemSource.FetchConfiguration<Day>
    ): Flow<Cacheable<Day>> = channelFlow {
        val schoolId = id.substringBefore("/").toInt()
        val date = LocalDate.parse(id.substringAfter("/"))
        val school = App.schoolSource.getById(schoolId.toString(), School.Fetch()).first { it is Cacheable.Loaded }.toValueOrNull()

        weekRepository.getBySchool(schoolId).collectLatest { weeks ->
            val dayWeek = weeks.firstOrNull { date in it.start..it.end } ?: weeks.last { it.start < date }
            dayRepository.getHolidays(schoolId).map { it.map { holiday -> holiday.date } }.collectLatest { holidays ->
                val findNextRegularSchoolDayAfter: (LocalDate) -> LocalDate? = findNextRegularSchoolDayAfter@{ startDate ->
                    if (holidays.isEmpty()) return@findNextRegularSchoolDayAfter null
                    var nextSchoolDay = startDate + 1.days
                    while (holidays.maxOf { it } > nextSchoolDay) {
                        nextSchoolDay += 1.days
                        if (holidays.none { it == nextSchoolDay } && nextSchoolDay.dayOfWeek.isoDayNumber <= ((school as? School.IndiwareSchool)?.daysPerWeek ?: 5)) break
                    }
                    nextSchoolDay
                }

                timetableRepository.getForSchool(
                    schoolId = schoolId,
                    dayOfWeek = date.dayOfWeek,
                    minWeekIndex = dayWeek.weekIndex,
                ).collectLatest { timetable ->
                    val day = MutableStateFlow(Day(
                        id = id,
                        date = date,
                        school = Cacheable.Uninitialized(schoolId.toString()),
                        week = dayWeek.let { Cacheable.Uninitialized(it.id) },
                        info = null,
                        dayType =
                        if (date in holidays) Day.DayType.HOLIDAY
                        else if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
                            var friday = date
                            while (friday.dayOfWeek != DayOfWeek.FRIDAY) {
                                friday -= 1.days
                            }
                            var monday = date
                            while (monday.dayOfWeek != DayOfWeek.MONDAY) {
                                monday += 1.days
                            }
                            if (holidays.any { it == friday } || holidays.any { it == monday }) Day.DayType.HOLIDAY
                            else Day.DayType.WEEKEND
                        }
                        else if (timetable.isNotEmpty()) Day.DayType.REGULAR
                        else Day.DayType.UNKNOWN,
                        timetable = timetable.map { Cacheable.Uninitialized(it.toHexString()) },
                        substitutionPlan = emptyList(),
                        nextSchoolDay = findNextRegularSchoolDayAfter(date)?.let { Cacheable.Uninitialized("$schoolId/$it") }
                    ))
                    launch { day.collectLatest { send(Cacheable.Loaded(it)) } }
                    if (configuration is Day.Fetch) {
                        if (configuration.timetable is Lesson.Fetch) launch {
                            combine(timetable.map { App.timetableSource.getById(it.toHexString(), configuration.timetable) }) { it.filter { (it is Cacheable.Loaded && configuration.timetable.onlyIf(it.value)) || it !is Cacheable.Loaded }.toList() }.collectLatest {
                                day.value = day.value.copy(timetable = it)
                            }
                        }
                        if (configuration.school is School.Fetch) launch {
                            App.schoolSource.getById(schoolId.toString(), configuration.school).collect {
                                day.value = day.value.copy(school = it)
                            }
                        }
                        if (configuration.week is Week.Fetch) launch {
                            App.weekSource.getById(dayWeek.id, configuration.week).collectLatest {
                                day.value = day.value.copy(week = it)
                            }
                        }
                        if (configuration.nextSchoolDay is Day.Fetch) launch {
                            day.value.nextSchoolDay?.getItemId()?.let { nextDayId ->
                                getById(nextDayId, configuration.nextSchoolDay).collectLatest {
                                    day.value = day.value.copy(nextSchoolDay = it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}