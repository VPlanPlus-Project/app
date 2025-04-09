package plus.vplan.app.feature.search.domain.usecase

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.AssessmentRepository

class GetAssessmentsForProfileUseCase(
    private val assessmentRepository: AssessmentRepository
) {
    operator fun invoke(profile: Profile.StudentProfile): Flow<List<Assessment>> {
        return assessmentRepository.getByProfile(profile.id)
    }
}