@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.feature.profile.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.model.getFirstValue
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository
import kotlin.uuid.ExperimentalUuidApi

class UpdateProfileLessonIndexUseCase(
    private val timetableRepository: TimetableRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository
) {
    suspend operator fun invoke(profile: Profile) {
        val school = profile.getSchool().getFirstValue()!!
        val substitutionPlanLessons = substitutionPlanRepository.getSubstitutionPlanBySchool(school.id).first().filter { it.isRelevantForProfile(profile) }
        substitutionPlanRepository.replaceLessonIndex(profile.id, substitutionPlanLessons.map { it.id }.toSet())

        val timetableLessons = timetableRepository.getTimetableForSchool(school.id).first().filter { it.isRelevantForProfile(profile) }
        timetableRepository.replaceLessonIndex(profile.id, timetableLessons.map { it.id }.toSet())
    }
}