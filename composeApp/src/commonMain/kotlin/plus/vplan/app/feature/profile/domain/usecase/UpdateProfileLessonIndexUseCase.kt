package plus.vplan.app.feature.profile.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.App
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository

class UpdateProfileLessonIndexUseCase(
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val dayRepository: DayRepository,
    private val timetableRepository: TimetableRepository
) {
    suspend operator fun invoke(profile: Profile) {
        val school = profile.getSchool().getFirstValue()!!
        val days = dayRepository.getBySchool(school.id).first()
            .filter { it.dayType == Day.DayType.REGULAR }

        days.forEach { day ->
            val substitutionPlanLessons = substitutionPlanRepository.getSubstitutionPlanBySchool(school.id, day.date).first()
                .mapNotNull { App.substitutionPlanSource.getById(it).getFirstValue() }
                .filter { lesson -> lesson.isRelevantForProfile(profile) }
            substitutionPlanRepository.dropCacheForProfile(profile.id)
            substitutionPlanRepository.createCacheForProfile(profile.id, substitutionPlanLessons.map { it.id })
        }

        timetableRepository.getTimetableForSchool(schoolId = school.id).first()
            .filter { it.isRelevantForProfile(profile) }
            .let {
                timetableRepository.dropCacheForProfile(profile.id)
                timetableRepository.createCacheForProfile(profile.id, it.map { lesson -> lesson.id })
            }
    }
}