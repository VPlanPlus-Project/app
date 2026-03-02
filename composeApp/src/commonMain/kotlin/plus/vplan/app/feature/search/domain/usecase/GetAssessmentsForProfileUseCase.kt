package plus.vplan.app.feature.search.domain.usecase

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.data.assessment.AssessmentRepository
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.Profile

class GetAssessmentsForProfileUseCase(
    private val assessmentRepository: AssessmentRepository
) {
    operator fun invoke(profile: Profile.StudentProfile): Flow<List<Assessment>> {
        return assessmentRepository.getByProfile(profile.id)
    }
}