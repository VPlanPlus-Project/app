package plus.vplan.app.feature.profile.settings.page.subject_instances.domain.usecase

import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.SubjectInstance

class SetProfileSubjectInstanceEnabledUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(
        profile: Profile.StudentProfile,
        subjectInstances: List<SubjectInstance>,
        enabled: Boolean
    ) {
        profileRepository.save(profile.copy(
            subjectInstanceConfiguration = subjectInstances
                .associateWith { enabled }
                .mapKeys { it.key.id }
        ))
    }

    suspend operator fun invoke(
        profile: Profile.StudentProfile,
        subjectInstance: SubjectInstance,
        enabled: Boolean
    ) {
        this(profile, listOf(subjectInstance), enabled)
    }
}