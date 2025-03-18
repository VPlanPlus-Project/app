package plus.vplan.app.feature.vpp_id.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.feature.sync.domain.usecase.schulverwalter.SyncGradesUseCase
import plus.vplan.app.utils.latest
import kotlin.uuid.Uuid

private val logger = Logger.withTag("AddVppIdUseCase")

class AddVppIdUseCase(
    private val vppIdRepository: VppIdRepository,
    private val keyValueRepository: KeyValueRepository,
    private val profileRepository: ProfileRepository,
    private val syncGradesUseCase: SyncGradesUseCase
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
        val profile = keyValueRepository.get(Keys.VPP_ID_LOGIN_LINK_TO_PROFILE).first()?.let { profileId ->
            profileRepository.getById(Uuid.parseHex(profileId)).first()
        } ?: profileRepository
            .getAll().latest()
            .filterIsInstance<Profile.StudentProfile>()
            .first { it.groupId in vppId.data.groups }
        keyValueRepository.delete(Keys.VPP_ID_LOGIN_LINK_TO_PROFILE)
        profileRepository.updateVppId(profile.id, vppId.data.id)
        syncGradesUseCase(false)
        return Response.Success(vppId.data)
    }
}