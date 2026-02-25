package plus.vplan.app.feature.home.domain.usecase

import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import kotlin.uuid.Uuid

class GetCurrentProfileUseCase(
    private val keyValueRepository: KeyValueRepository,
    private val profileRepository: ProfileRepository
) {
    operator fun invoke() = channelFlow {
        keyValueRepository.get(Keys.CURRENT_PROFILE).collectLatest { currentProfileId ->
            if (currentProfileId != null) profileRepository.getById(Uuid.parseHex(currentProfileId))
                .distinctUntilChanged()
                .collectLatest { send(it) }
        }
    }
}