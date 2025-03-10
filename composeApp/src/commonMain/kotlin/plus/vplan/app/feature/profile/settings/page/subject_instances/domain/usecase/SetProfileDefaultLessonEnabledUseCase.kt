package plus.vplan.app.feature.profile.settings.page.subject_instances.domain.usecase

import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.ProfileRepository

class SetProfileDefaultLessonEnabledUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(
        profile: Profile.StudentProfile,
        defaultLessons: List<DefaultLesson>,
        enabled: Boolean
    ) {
        profileRepository.setDefaultLessonEnabled(profile.id, defaultLessons.map { it.id }, enabled)
    }

    suspend operator fun invoke(
        profile: Profile.StudentProfile,
        defaultLesson: DefaultLesson,
        enabled: Boolean
    ) {
        this(profile, listOf(defaultLesson), enabled)
    }
}