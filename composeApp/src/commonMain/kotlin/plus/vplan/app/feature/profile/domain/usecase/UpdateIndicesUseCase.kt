package plus.vplan.app.feature.profile.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository

class UpdateIndicesUseCase(
    private val updateProfileLessonIndexUseCase: UpdateProfileLessonIndexUseCase,
    private val updateProfileAssessmentIndexUseCase: UpdateProfileAssessmentIndexUseCase,
    private val updateProfileHomeworkIndexUseCase: UpdateProfileHomeworkIndexUseCase,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val timetableRepository: TimetableRepository
) {
    suspend operator fun invoke(profile: Profile) {
        val substitutionPlanVersion = substitutionPlanRepository.getCurrentVersion().first()
        val timetableVersion = timetableRepository.getCurrentVersion().first()

        updateProfileLessonIndexUseCase(profile, substitutionPlanVersion, timetableVersion)
        updateProfileHomeworkIndexUseCase(profile)
        updateProfileAssessmentIndexUseCase(profile)
    }
}