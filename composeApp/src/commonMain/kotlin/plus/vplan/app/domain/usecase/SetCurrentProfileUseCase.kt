package plus.vplan.app.domain.usecase

import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys

class SetCurrentProfileUseCase(
    private val keyValueRepository: KeyValueRepository
) {
    suspend operator fun invoke(profile: Profile) {
        keyValueRepository.set(Keys.CURRENT_PROFILE, profile.id.toHexString())
    }
}