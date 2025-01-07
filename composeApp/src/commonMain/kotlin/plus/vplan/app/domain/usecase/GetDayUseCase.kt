package plus.vplan.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Week

class GetDayUseCase {

    private fun configuration(profile: Profile) = Day.Fetch(
        school = School.Fetch(),
        week = Week.Fetch(),
        timetable = Lesson.Fetch(onlyIf = { profile.isLessonRelevant(it) }),
        nextSchoolDay = Day.Fetch(
            week = Week.Fetch(),
            timetable = Lesson.Fetch(onlyIf = { profile.isLessonRelevant(it) })
        )
    )

    operator fun invoke(profile: Profile, date: LocalDate): Flow<Day> {
        require(profile.isConfigSatisfied(Profile.Fetch(studentProfile = Profile.StudentProfile.Fetch(defaultLessons = DefaultLesson.Fetch(course = Course.Fetch()))), allowLoading = false))
        val configuration = configuration(profile)
        return App.daySource.getById("${profile.school.getItemId()}/$date", configuration)
            .filterIsInstance<Cacheable.Loaded<Day>>()
            .filter { it.isConfigSatisfied(configuration, false) }
            .map { dayEmission ->
                dayEmission.value.copy(
                    timetable = dayEmission.value.timetable.filterProfile(profile),
                    nextSchoolDay = (dayEmission.value.nextSchoolDay as Cacheable.Loaded).value.copy(
                        timetable = dayEmission.value.nextSchoolDay.value.timetable.filterProfile(profile)
                    ).let { Cacheable.Loaded(it) }
                )
            }
    }
}

private fun List<Cacheable<Lesson>>.filterProfile(profile: Profile) = this
    .mapNotNull { it.toValueOrNull() }
    .filter { profile.isLessonRelevant(it) }
    .map { Cacheable.Loaded(it) }