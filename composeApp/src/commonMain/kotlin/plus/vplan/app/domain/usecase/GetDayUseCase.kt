package plus.vplan.app.domain.usecase

import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.SchoolDay
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

class GetDayUseCase(
    private val dayRepository: DayRepository,
    private val timetableRepository: TimetableRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository
) {
    operator fun invoke(profile: Profile, date: LocalDate) = channelFlow {
        dayRepository.getBySchool(date, profile.school.id).collectLatest { day ->
            combine(
                timetableRepository.getTimetableForSchool(schoolId = profile.school.id).map { timetable -> timetable.filter { it.date.dayOfWeek == date.dayOfWeek } },
                substitutionPlanRepository.getSubstitutionPlanBySchool(schoolId = profile.school.id, date = date),
                dayRepository.getHolidays(profile.school.id)
            ) { timetable, substitutionPlan, holidays ->
                val findNextRegularSchoolDayAfter: (LocalDate) -> LocalDate? = { startDate ->
                    var nextSchoolDay = startDate + 1.days
                    while (holidays.maxOf { it.date } > nextSchoolDay) {
                        nextSchoolDay += 1.days
                        if (holidays.none { it.date == nextSchoolDay } && nextSchoolDay.dayOfWeek.isoDayNumber <= ((profile.school as? School.IndiwareSchool)?.daysPerWeek ?: 5)) break
                    }
                    nextSchoolDay
                }
                holidays.firstOrNull { it.date == date }?.let { holiday ->
                    val nextSchoolDay = findNextRegularSchoolDayAfter(date)
                    return@combine SchoolDay.Holiday(
                        id = holiday.id,
                        date = date,
                        school = profile.school,
                        nextRegularSchoolDay = nextSchoolDay
                    )
                }
                if (day == null) return@combine SchoolDay.Unknown(date, profile.school)
                SchoolDay.NormalDay(
                    id = day.id,
                    date = day.date,
                    school = profile.school,
                    week = day.week,
                    info = day.info,
                    lessons = substitutionPlan.filter { profile.isLessonRelevant(it) }.ifEmpty {
                        timetable.filter { profile.isLessonRelevant(it) }
                    },
                    nextRegularSchoolDay = findNextRegularSchoolDayAfter(day.date)
                )
            }.collect { send(it) }
        }
    }
}