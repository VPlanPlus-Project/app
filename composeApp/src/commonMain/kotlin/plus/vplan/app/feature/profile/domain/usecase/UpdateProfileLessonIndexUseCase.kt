package plus.vplan.app.feature.profile.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository

class UpdateProfileLessonIndexUseCase(
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val timetableRepository: TimetableRepository
) {
    suspend operator fun invoke(profile: Profile) {
        val school = profile.getSchool().getFirstValue()!!

        substitutionPlanRepository.getSubstitutionPlanBySchool(school.id).first().let {
            substitutionPlanRepository.dropCacheForProfile(profile.id)
            substitutionPlanRepository.createCacheForProfile(profile.id, it.filter { lesson -> lesson.isRelevantForProfile(profile) }.map { it.id })
        }

        timetableRepository.getTimetableForSchool(schoolId = school.id).first()
            .filter { it.isRelevantForProfile(profile) }
            .let {
                timetableRepository.dropCacheForProfile(profile.id)
                timetableRepository.createCacheForProfile(profile.id, it.map { lesson -> lesson.id })
            }
    }
}