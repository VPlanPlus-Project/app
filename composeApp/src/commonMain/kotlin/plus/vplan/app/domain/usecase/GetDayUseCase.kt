package plus.vplan.app.domain.usecase

import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.SchoolDay
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository

class GetDayUseCase(
    private val dayRepository: DayRepository,
    private val timetableRepository: TimetableRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository
) {
    operator fun invoke(profile: Profile, date: LocalDate) = channelFlow<SchoolDay> {
        dayRepository.getBySchool(date, profile.school.id).collectLatest { day ->
            if (day == null) return@collectLatest
            combine(
                timetableRepository.getTimetableForSchool(schoolId = profile.school.id),
                substitutionPlanRepository.getSubstitutionPlanBySchool(schoolId = profile.school.id, date = date)
            ) { timetable, substitutionPlan ->
                SchoolDay.NormalDay(
                    id = day.id,
                    date = day.date,
                    school = profile.school,
                    week = day.week,
                    info = day.info,
                    lessons = substitutionPlan.filter { profile.isLessonRelevant(it) }.ifEmpty {
                        timetable.filter { profile.isLessonRelevant(it) }
                    }
                )
            }.collect { send(it) }
        }
    }
}