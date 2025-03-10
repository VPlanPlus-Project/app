package plus.vplan.app.feature.search.domain.usecase

import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.AssessmentRepository

class GetAssessmentsForProfileUseCase(
    private val assessmentRepository: AssessmentRepository
) {
    suspend operator fun invoke(profile: Profile.StudentProfile) = assessmentRepository.getAll()
        .map { items ->
            items
                .filter {
                    (it.creator is AppEntity.Profile && it.creator.id == profile.id) || (it.creator is AppEntity.VppId && it.creator.id == profile.vppIdId) ||
                            (profile.group in it.subjectInstance.getFirstValue()?.groups.orEmpty() && profile.subjectInstanceConfiguration.any { (id, allowed) -> it.subjectInstanceId == id && allowed })
                }.onEach {
                    it.getCreatedByProfileItem()
                    it.getCreatedByVppIdItem()
                }
                .sortedByDescending { it.createdAt }
        }
}