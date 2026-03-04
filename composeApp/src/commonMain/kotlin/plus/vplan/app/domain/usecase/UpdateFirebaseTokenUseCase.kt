package plus.vplan.app.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.data.vpp_id.VppIdRepository
import plus.vplan.app.core.model.NetworkException
import plus.vplan.app.core.model.Profile

class UpdateFirebaseTokenUseCase(
    private val profileRepository: ProfileRepository,
    private val vppIdRepository: VppIdRepository,
    private val keyVppIdRepository: KeyValueRepository
) {
    suspend operator fun invoke(token: String) {
        keyVppIdRepository.set(Keys.FIREBASE_TOKEN, token)
        val profiles = profileRepository.getAll().first()
        var success = true
        profiles.forEach { profile ->
            if (profile is Profile.StudentProfile) {
                if (profile.vppId != null) {
                    try {
                        vppIdRepository.updateFirebaseToken(profile.vppId!!, token)
                    } catch (e: NetworkException) {
                        success = false
                    }
                } else {
                    // TODO
                }
            }
        }
        keyVppIdRepository.set(Keys.FIREBASE_TOKEN_UPLOAD_SUCCESS, success.toString())
    }
}
