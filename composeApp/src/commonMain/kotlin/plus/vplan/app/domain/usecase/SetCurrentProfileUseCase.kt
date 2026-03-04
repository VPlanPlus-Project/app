package plus.vplan.app.domain.usecase

import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.core.model.Profile
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