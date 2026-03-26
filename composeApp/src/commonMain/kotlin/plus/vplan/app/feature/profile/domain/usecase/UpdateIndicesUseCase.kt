package plus.vplan.app.feature.profile.domain.usecase

import plus.vplan.app.core.model.Profile

class UpdateIndicesUseCase(
    private val updateProfileAssessmentIndexUseCase: UpdateProfileAssessmentIndexUseCase,
    private val updateProfileHomeworkIndexUseCase: UpdateProfileHomeworkIndexUseCase,
) {
    suspend operator fun invoke(profile: Profile) {
        updateProfileHomeworkIndexUseCase(profile)
        updateProfileAssessmentIndexUseCase(profile)
    }
}