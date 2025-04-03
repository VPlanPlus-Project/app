package plus.vplan.app.feature.profile.domain.usecase

import plus.vplan.app.domain.model.Profile

class UpdateIndicesUseCase(
    private val updateProfileLessonIndexUseCase: UpdateProfileLessonIndexUseCase,
    private val updateAssessmentIndicesUseCase: UpdateAssessmentIndicesUseCase,
    private val updateProfileHomeworkIndexUseCase: UpdateProfileHomeworkIndexUseCase
) {
    suspend operator fun invoke(profile: Profile) {
        updateProfileLessonIndexUseCase(profile)
        updateProfileHomeworkIndexUseCase(profile)
        updateAssessmentIndicesUseCase(profile)
    }
}