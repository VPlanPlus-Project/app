package plus.vplan.app.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import plus.vplan.app.App
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import kotlin.uuid.Uuid

class GetCurrentProfileUseCase(
    private val keyValueRepository: KeyValueRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke() = keyValueRepository.get(Keys.CURRENT_PROFILE).filterNotNull().flatMapLatest { profileId ->
            App.profileSource.getById(Uuid.parseHex(profileId))
                .filterIsInstance<CacheState.Done<Profile>>()
                .mapNotNull { it.data }
    }
}