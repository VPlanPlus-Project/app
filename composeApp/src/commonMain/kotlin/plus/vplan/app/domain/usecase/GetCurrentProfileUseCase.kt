package plus.vplan.app.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.core.data.profile.ProfileRepository
import kotlin.uuid.Uuid

class GetCurrentProfileUseCase(
    private val keyValueRepository: KeyValueRepository,
    private val profileRepository: ProfileRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke() = keyValueRepository.get(Keys.CURRENT_PROFILE).filterNotNull().flatMapLatest { profileId ->
            profileRepository.getById(Uuid.parseHex(profileId))
                .filterNotNull()
    }
}