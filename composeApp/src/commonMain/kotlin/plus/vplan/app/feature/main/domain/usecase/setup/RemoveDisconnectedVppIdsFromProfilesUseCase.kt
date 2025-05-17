package plus.vplan.app.feature.main.domain.usecase.setup

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.ProfileRepository

/**
 * If for whatever reason a vpp.ID is not considered an active vpp.ID anymore, this use case takes care
 * of removing the vpp.ID from all profiles that are still connected to it so that no other problems
 * should occur.
 */
class RemoveDisconnectedVppIdsFromProfilesUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke() {
        profileRepository.getAll().first()
            .filterIsInstance<Profile.StudentProfile>()
            .filter { it.vppIdId != null }
            .forEach { profile ->
                val vppId = profile.vppId!!.getFirstValue()
                if (vppId == null || vppId !is VppId.Active) profileRepository.updateVppId(profile.id, null)
            }
    }
}