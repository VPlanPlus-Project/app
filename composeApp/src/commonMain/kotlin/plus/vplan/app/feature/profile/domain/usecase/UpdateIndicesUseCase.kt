package plus.vplan.app.feature.profile.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.timetable.TimetableRepository
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.sync.domain.usecase.UpdateProfileLessonIndexUseCase

class UpdateIndicesUseCase(
    private val updateProfileLessonIndexUseCase: UpdateProfileLessonIndexUseCase,
    private val updateProfileAssessmentIndexUseCase: UpdateProfileAssessmentIndexUseCase,
    private val updateProfileHomeworkIndexUseCase: UpdateProfileHomeworkIndexUseCase,
    private val timetableRepository: TimetableRepository
) {
    suspend operator fun invoke(profile: Profile) {
        val timetableVersion = timetableRepository.getCurrentVersion().first()

        updateProfileLessonIndexUseCase(profile, timetableVersion)
        updateProfileHomeworkIndexUseCase(profile)
        updateProfileAssessmentIndexUseCase(profile)
    }
}