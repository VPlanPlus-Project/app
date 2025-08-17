package plus.vplan.app.data.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.service.ProfileService
import kotlin.uuid.Uuid

class ProfileServiceImpl(
    private val keyValueRepository: KeyValueRepository,
    private val profileRepository: ProfileRepository
) : ProfileService {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getCurrentProfile(): Flow<Profile?> {
        return keyValueRepository.get(Keys.CURRENT_PROFILE).flatMapLatest { profileId ->
            if (profileId == null) flowOf(null)
            else {
                val profileId = Uuid.parse(profileId)
                profileRepository.getById(profileId)
            }
        }
    }
}