package plus.vplan.app.feature.profile.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.AssessmentRepository

class UpdateAssessmentIndicesUseCase(
    private val assessmentRepository: AssessmentRepository
) {
    suspend operator fun invoke(profile: Profile) {
        assessmentRepository.getAll().first()
            .filter {
                (it.creator is AppEntity.Profile && it.creator.id == profile.id) ||
                        (it.creator is AppEntity.VppId && it.creator.id == (profile as? Profile.StudentProfile)?.vppIdId) ||
                        (it.subjectInstance.getFirstValueOld()?.id in (profile as? Profile.StudentProfile)?.subjectInstanceConfiguration.orEmpty().filterValues { it }.keys && profile is Profile.StudentProfile)
            }
            .let { relevantAssessments ->
                assessmentRepository.dropIndicesForProfile(profile.id)
                assessmentRepository.createCacheForProfile(profile.id, relevantAssessments.map { it.id })
            }
    }
}