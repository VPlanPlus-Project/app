package plus.vplan.app.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.VppIdRepository

class UpdateFirebaseTokenUseCase(
    private val profileRepository: ProfileRepository,
    private val vppIdRepository: VppIdRepository,
    private val groupRepository: GroupRepository,
    private val keyVppIdRepository: KeyValueRepository
) {
    suspend operator fun invoke(token: String) {
        keyVppIdRepository.set(Keys.FIREBASE_TOKEN, token)
        val profiles = profileRepository.getAll().first()
        var success = true
        profiles.forEach { profile ->
            if (profile is Profile.StudentProfile) {
                if (profile.vppId != null) {
                    vppIdRepository.updateFirebaseToken(profile.vppId, token).let {
                        if (it != null) success = false
                    }
                } else {
                    groupRepository.updateFirebaseToken(profile.group, token).let {
                        if (it != null) success = false
                    }
                }
            }
        }
        keyVppIdRepository.set(Keys.FIREBASE_TOKEN_UPLOAD_SUCCESS, success.toString())
    }
}