package plus.vplan.app.feature.profile.settings.domain.usecase

import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.ProfileRepository

class RenameProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(profile: Profile, newName: String) {
        profileRepository.updateDisplayName(profile.id, newName)
    }
}