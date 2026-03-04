@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.feature.profile.page.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.model.Profile
import kotlin.uuid.Uuid

class GetCurrentProfileUseCase(
    private val keyValueRepository: KeyValueRepository,
    private val profileRepository: ProfileRepository,
) {

    operator fun invoke(): Flow<Profile> = keyValueRepository.get(Keys.CURRENT_PROFILE)
        .filterNotNull()
        .flatMapLatest { currentProfileId ->
            profileRepository.getById((Uuid.parse(currentProfileId))).filterNotNull()
        }
}