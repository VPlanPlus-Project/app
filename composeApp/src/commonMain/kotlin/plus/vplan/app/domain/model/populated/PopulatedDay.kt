@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.domain.model.populated

import co.touchlab.kermit.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.isoDayNumber
import plus.vplan.app.core.data.holiday.HolidayRepository
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.Day
import plus.vplan.app.core.model.Holiday
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.core.model.Timetable
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

private val LOGGER = Logger.withTag("DayPopulator")

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
    private val timetableRepository: plus.vplan.app.core.data.timetable.TimetableRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
) {
    fun populateSingle(day: Day, context: PopulationContext): Flow<PopulatedDay> {
        val holidays = holidayRepository.getBySchool(day.school)

        val assessments = when (context) {
            is PopulationContext.Profile -> assessmentRepository.getByProfile(profileId = context.profile.id, day.date)
            else -> assessmentRepository.getByDate(day.date)
        }

        val homework = when (context) {
            is PopulationContext.Profile -> homeworkRepository.getByProfile(profileId = context.profile.id, day.date)
            else -> homeworkRepository.getByDate(day.date)
        }

        val timetable: Flow<List<Lesson.TimetableLesson>> = timetableRepository.getTimetables(context.school)
            .distinctUntilChangedBy { timetables ->
                timetables
                    .filter { it.week.start <= day.date && it.dataState == Timetable.HasData.Yes }
                    .maxByOrNull { it.week.weekIndex }
                    ?.let { "${it.id}|${it.week.weekIndex}" }
            }
            .flatMapLatest { timetables ->
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
            .getSubstitutionPlanBySchool(
                schoolId = context.school.id,
                date = day.date,
                version = null
            ).map { lessons ->
                LOGGER.d { "[${day.date}] raw substitution from DB: ${lessons.size} lessons, groupIds per lesson: ${lessons.map { it.groupIds }}" }
                if (context !is PopulationContext.Profile) return@map lessons
                val profile = context.profile
                val filtered = lessons.filter { lesson -> lessonMatchesProfile(lesson, profile) }
                LOGGER.d { "[${day.date}] after lessonMatchesProfile (profile.group=${if (profile is plus.vplan.app.core.model.Profile.StudentProfile) profile.group.id else "teacher"}): ${filtered.size} lessons" }
                filtered
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
        }.distinctUntilChangedBy { d ->
            val timetableIds = d.timetable.map { it.id }.sorted()
            val substitutionIds = d.substitution.map { it.id }.sorted()
            val homeworkIds = d.homework.map { it.id }.sorted()
            val assessmentIds = d.assessments.map { it.id }.sorted()
            "${d.day.id}|${d.day.dayType}|${d.day.info}|${d.holiday?.date}|$timetableIds|$substitutionIds|$homeworkIds|$assessmentIds"
        }
    }
}

private fun lessonMatchesProfile(
    lesson: Lesson.SubstitutionPlanLesson,
    profile: plus.vplan.app.core.model.Profile
): Boolean {
    if (profile is plus.vplan.app.core.model.Profile.StudentProfile) {
        if (profile.group.id !in lesson.groupIds) return false
        if (lesson.subjectInstanceId != null &&
            profile.subjectInstanceConfiguration[lesson.subjectInstanceId] == false) return false
    }
    if (profile is plus.vplan.app.core.model.Profile.TeacherProfile) {
        if (profile.teacher.id !in lesson.teacherIds) return false
    }
    return true
}
