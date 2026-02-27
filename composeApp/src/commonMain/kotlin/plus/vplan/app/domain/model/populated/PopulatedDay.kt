@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.domain.model.populated

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.isoDayNumber
import plus.vplan.app.core.data.holiday.HolidayRepository
import plus.vplan.app.core.data.week.WeekRepository
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.Day
import plus.vplan.app.core.model.Holiday
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.core.model.Timetable
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

data class PopulatedDay(
    val day: Day,
    val assessments: List<Assessment>,
    val homework: List<Homework>,
    val timetable: List<Lesson.TimetableLesson>,
    val substitution: List<Lesson.SubstitutionPlanLesson>,
    val holiday: Holiday?
)

class DayPopulator(
    private val holidayRepository: HolidayRepository,
    private val assessmentRepository: AssessmentRepository,
    private val homeworkRepository: HomeworkRepository,
    private val timetableRepository: TimetableRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val weekRepository: WeekRepository,
) {
    fun populateSingle(day: Day, context: PopulationContext): Flow<PopulatedDay> {
        val holidays = holidayRepository
            .getBySchool(day.school)

        val assessments = when (context) {
            is PopulationContext.Profile -> assessmentRepository.getByProfile(profileId = context.profile.id, day.date)
            else -> assessmentRepository.getByDate(day.date)
        }

        val homework = when (context) {
            is PopulationContext.Profile -> homeworkRepository.getByProfile(profileId = context.profile.id, day.date)
            else -> homeworkRepository.getByDate(day.date)
        }

        val timetable = timetableRepository.getTimetables(context.school).flatMapLatest { timetables ->
            val timetableWeek = timetables
                .filter { it.week.start <= day.date }
                .filter { it.dataState == Timetable.HasData.Yes }
                .maxByOrNull { it.week.weekIndex }
                ?.week

            when (context) {
                is PopulationContext.Profile -> timetableRepository.getForProfile(
                    profile = context.profile,
                    weekIndex = timetableWeek?.weekIndex ?: 0,
                    dayOfWeek = day.date.dayOfWeek
                )
                is PopulationContext.School -> timetableRepository.getForSchool(
                    schoolId = context.school.id,
                    weekIndex = timetableWeek?.weekIndex ?: 0,
                    dayOfWeek = day.date.dayOfWeek
                )
            }.map { it.toList() }
        }

        val substitution = substitutionPlanRepository
            .getCurrentVersion()
            .flatMapLatest { substitutionPlanVersion ->
                when (context) {
                    is PopulationContext.Profile -> substitutionPlanRepository.getForProfile(
                        profile = context.profile,
                        date = day.date,
                        version = substitutionPlanVersion
                    )
                    is PopulationContext.School -> substitutionPlanRepository.getSubstitutionPlanBySchool(
                        schoolId = context.school.id,
                        date = day.date,
                        version = substitutionPlanVersion
                    )
                }
            }

        return combine(
            assessments,
            homework,
            timetable,
            substitution,
            holidays
        ) { assessments, homework, timetable, substitution, holidays ->
            val holiday = holidays.firstOrNull { it.date == day.date }
            PopulatedDay(
                day = day.copy(
                    dayType = if (holiday != null) Day.DayType.HOLIDAY else day.dayType,
                    nextSchoolDay = run {
                        var result = day.date + 1.days
                        repeat(30) {
                            if (result.dayOfWeek.isoDayNumber > day.school.daysPerWeek || result in holidays.map { it.date }) result += 1.days
                            else return@run result
                        }
                        null
                    },
                ),
                assessments = assessments,
                homework = homework,
                timetable = timetable,
                substitution = substitution,
                holiday = holiday
            )
        }
    }
}
