package plus.vplan.app.feature.home.domain.usecase

import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import kotlin.uuid.Uuid

class GetCurrentProfileUseCase(
    private val keyValueRepository: KeyValueRepository
) {
    operator fun invoke() = channelFlow {
        keyValueRepository.get(Keys.CURRENT_PROFILE).collectLatest { currentProfileId ->
            if (currentProfileId != null) App.profileSource.getById(Uuid.parseHex(currentProfileId))
                .filterIsInstance<CacheState.Done<Profile>>()
                .map { it.data }
                .collectLatest { send(it) }
        }
    }
}