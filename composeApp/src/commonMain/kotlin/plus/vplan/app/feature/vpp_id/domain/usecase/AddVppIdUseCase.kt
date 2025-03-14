package plus.vplan.app.feature.vpp_id.domain.usecase

import co.touchlab.kermit.Logger
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.utils.latest

private val logger = Logger.withTag("AddVppIdUseCase")

class AddVppIdUseCase(
    private val vppIdRepository: VppIdRepository,
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(token: String): Response<VppId.Active> {
        val accessToken = vppIdRepository.getAccessToken(token)
        if (accessToken is Response.Error) {
            logger.e { "Error getting access token: $accessToken" }
            return accessToken
        }
        if (accessToken !is Response.Success) throw IllegalStateException("Unexpected response type")
        val vppId = vppIdRepository.getUserByToken(accessToken.data)
        if (vppId !is Response.Success) return vppId
        val profile = profileRepository
            .getAll().latest()
            .filterIsInstance<Profile.StudentProfile>()
            .first { it.groupId in vppId.data.groups }
        profileRepository.updateVppId(profile.id, vppId.data.id)
        return Response.Success(vppId.data)
    }
}