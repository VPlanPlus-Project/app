package plus.vplan.app.domain.usecase

import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import kotlin.uuid.Uuid

class SetCurrentProfileUseCase(
    private val keyValueRepository: KeyValueRepository
) {
    suspend operator fun invoke(profile: Profile) {
        keyValueRepository.set(Keys.CURRENT_PROFILE, profile.id.toHexString())
    }

    suspend operator fun invoke(profileId: Uuid) {
        keyValueRepository.set(Keys.CURRENT_PROFILE, profileId.toHexString())
    }
}