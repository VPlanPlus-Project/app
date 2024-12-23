package plus.vplan.app.domain.usecase

import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.ProfileRepository
import kotlin.uuid.Uuid

class GetCurrentProfileUseCase(
    private val keyValueRepository: KeyValueRepository,
    private val profileRepository: ProfileRepository
) {

    operator fun invoke() = channelFlow {
        keyValueRepository.get(Keys.CURRENT_PROFILE).collectLatest { currentProfileId ->
            if (currentProfileId.isNullOrBlank()) return@collectLatest send(null)
            profileRepository.getById(Uuid.parseHex(currentProfileId)).collect { profile ->
                send(profile)
            }
        }
    }
}