package plus.vplan.app.feature.profile.settings.page.subject_instances.domain.usecase

import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.ProfileRepository

class SetProfileSubjectInstanceEnabledUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(
        profile: Profile.StudentProfile,
        subjectInstances: List<SubjectInstance>,
        enabled: Boolean
    ) {
        profileRepository.setSubjectInstancesEnabled(profile.id, subjectInstances.map { it.id }, enabled)
    }

    suspend operator fun invoke(
        profile: Profile.StudentProfile,
        subjectInstance: SubjectInstance,
        enabled: Boolean
    ) {
        this(profile, listOf(subjectInstance), enabled)
    }
}