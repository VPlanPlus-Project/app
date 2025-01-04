package plus.vplan.app.domain.usecase

import kotlinx.coroutines.flow.channelFlow
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.model.Day
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

    operator fun invoke(profile: Profile, date: LocalDate) = channelFlow {
        val configuration = configuration(profile)
        App.daySource.getById("${profile.school.getItemId()}/$date", configuration).collect { cachedDay ->
            if (cachedDay.isConfigSatisfied(configuration, false)) cachedDay.toValueOrNull()?.let { day -> send(day) }
        }
    }
}