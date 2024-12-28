package plus.vplan.app.domain.usecase

import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.SchoolDay
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.utils.minus
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

class GetDayUseCase(
    private val dayRepository: DayRepository,
    private val timetableRepository: TimetableRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val weekRepository: WeekRepository
) {
    operator fun invoke(profile: Profile, date: LocalDate) = channelFlow {
        dayRepository.getBySchool(date, profile.school.id).collectLatest { day ->
            combine(
                timetableRepository.getTimetableForSchool(schoolId = profile.school.id).map { timetable -> timetable.filter { it.dayOfWeek == date.dayOfWeek && profile.isLessonRelevant(it) } },
                substitutionPlanRepository.getSubstitutionPlanBySchool(schoolId = profile.school.id, date = date),
                dayRepository.getHolidays(profile.school.id),
                weekRepository.getBySchool(profile.school.id)
            ) { timetable, substitutionPlan, holidays, weeks ->
                val dayWeek = weeks.firstOrNull { date in it.start..it.end }
                val findNextRegularSchoolDayAfter: (LocalDate) -> LocalDate? = findNextRegularSchoolDayAfter@{ startDate ->
                    if (holidays.isEmpty()) return@findNextRegularSchoolDayAfter null
                    var nextSchoolDay = startDate + 1.days
                    while (holidays.maxOf { it.date } > nextSchoolDay) {
                        nextSchoolDay += 1.days
                        if (holidays.none { it.date == nextSchoolDay } && nextSchoolDay.dayOfWeek.isoDayNumber <= ((profile.school as? School.IndiwareSchool)?.daysPerWeek ?: 5)) break
                    }
                    nextSchoolDay
                }

                // is day a holiday?
                holidays.firstOrNull { it.date == date }?.let {
                    val nextSchoolDay = findNextRegularSchoolDayAfter(date)
                    return@combine SchoolDay.Holiday(
                        id = "${profile.school.id}/$date",
                        date = date,
                        school = profile.school,
                        nextRegularSchoolDay = nextSchoolDay
                    )
                }

                // is d day a weekend?
                if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
                    var friday = date
                    while (friday.dayOfWeek != DayOfWeek.FRIDAY) {
                        friday -= 1.days
                    }
                    var monday = date
                    while (monday.dayOfWeek != DayOfWeek.MONDAY) {
                        monday += 1.days
                    }
                    if (holidays.any { it.date == friday } || holidays.any { it.date == monday }) {
                        val nextSchoolDay = findNextRegularSchoolDayAfter(date)
                        return@combine SchoolDay.Holiday(
                            id = "${profile.school.id}/$date",
                            date = date,
                            school = profile.school,
                            nextRegularSchoolDay = nextSchoolDay
                        )
                    } else {
                        return@combine SchoolDay.Weekend(
                            id = "${profile.school.id}/$date",
                            date = date,
                            school = profile.school,
                            nextRegularSchoolDay = findNextRegularSchoolDayAfter(date)
                        )
                    }
                }

                if (dayWeek == null) return@combine SchoolDay.Unknown(
                    date = date,
                    school = profile.school
                )

                val timetableLessons = timetable.filter { timetableLesson ->
                    val a = timetableLesson.week.weekIndex
                    val b = dayWeek.weekIndex
                    a <= b
                }.let { timetableLessons ->
                    val maxWeek = timetableLessons.maxOfOrNull { it.week.weekIndex } ?: return@let emptyList()
                    timetableLessons.filter { it.week.weekIndex == maxWeek }
                }

                if (day == null) {
                    return@combine SchoolDay.NormalDay(
                        id = "${profile.school.id}/$date",
                        date = date,
                        school = profile.school,
                        week = dayWeek,
                        info = null,
                        lessons = timetableLessons,
                        nextRegularSchoolDay = findNextRegularSchoolDayAfter(date)
                    )
                }
                SchoolDay.NormalDay(
                    id = day.id,
                    date = day.date,
                    school = profile.school,
                    week = day.week,
                    info = day.info,
                    lessons = substitutionPlan.filter { profile.isLessonRelevant(it) }.ifEmpty { timetableLessons },
                    nextRegularSchoolDay = findNextRegularSchoolDayAfter(day.date)
                )
            }.collect { send(it) }
        }
    }
}