package plus.vplan.app.feature.profile.settings.page.main.domain.usecase

import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.model.Profile

class RenameProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(profile: Profile, newName: String) {
        when (profile) {
            is Profile.StudentProfile -> profile.copy(name = newName)
            is Profile.TeacherProfile -> profile.copy(name = newName)
            else -> throw IllegalArgumentException("Invalid profile type")
        }
            .let { newProfile -> profileRepository.save(newProfile) }
    }
}