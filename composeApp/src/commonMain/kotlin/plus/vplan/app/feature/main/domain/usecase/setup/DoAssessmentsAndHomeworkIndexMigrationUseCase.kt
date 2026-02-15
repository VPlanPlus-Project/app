package plus.vplan.app.feature.main.domain.usecase.setup

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.feature.profile.domain.usecase.UpdateAssessmentIndicesUseCase
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileHomeworkIndexUseCase

class DoAssessmentsAndHomeworkIndexMigrationUseCase(
    private val profileRepository: ProfileRepository,
    private val keyValueRepository: KeyValueRepository,
    private val updateAssessmentIndicesUseCase: UpdateAssessmentIndicesUseCase,
    private val updateHomeworkIndicesUseCase: UpdateProfileHomeworkIndexUseCase
) {
    suspend operator fun invoke() {
        profileRepository
            .getAll().first()
            .filterIsInstance<Profile.StudentProfile>()
            .forEach { profile ->
                updateAssessmentIndicesUseCase(profile)
                updateHomeworkIndicesUseCase(profile)
            }

        keyValueRepository.set(Keys.MIGRATION_FLAG_ASSESSMENTS_HOMEWORK_INDICES, "true")
    }
}