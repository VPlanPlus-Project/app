package plus.vplan.app.feature.vpp_id.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.AliasProvider
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.VppDbDto
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.service.GroupService
import plus.vplan.app.domain.service.SchoolService
import plus.vplan.app.feature.sync.domain.usecase.schulverwalter.SyncGradesUseCase
import kotlin.uuid.Uuid

private val logger = Logger.withTag("AddVppIdUseCase")

class AddVppIdUseCase(
    private val vppIdRepository: VppIdRepository,
    private val keyValueRepository: KeyValueRepository,
    private val profileRepository: ProfileRepository,
    private val syncGradesUseCase: SyncGradesUseCase,
    private val schoolService: SchoolService,
    private val groupService: GroupService
) {
    suspend operator fun invoke(token: String): Response<VppId.Active> {
        val accessToken = vppIdRepository.getAccessToken(token)
        if (accessToken is Response.Error) {
            logger.e { "Error getting access token: $accessToken" }
            return accessToken
        }
        if (accessToken !is Response.Success) throw IllegalStateException("Unexpected response type")

        val vppId = vppIdRepository.getUserByToken(accessToken.data)

        if (vppId is Response.Error) return vppId
        vppId as Response.Success

        schoolService.getSchoolFromAlias(Alias(AliasProvider.Vpp, vppId.data.schoolId.toString(), 1)).getFirstValue()
            ?: return Response.Error.Other("School not found for VPP ID: ${vppId.data.id}")

        val group = groupService.getGroupFromAlias(Alias(AliasProvider.Vpp, vppId.data.groupId.toString(), 1)).getFirstValue()!!

        vppIdRepository.upsert(VppDbDto.AppVppDbDto(
            id = vppId.data.id,
            username = vppId.data.username,
            groups = listOf(group.id),
            schulverwalterUserId = vppId.data.schulverwalterId,
            schulverwalterAccessToken = vppId.data.schulverwalterAccessToken,
            accessToken = accessToken.data
        ))

        val profile = keyValueRepository.get(Keys.VPP_ID_LOGIN_LINK_TO_PROFILE).first()?.let { profileId ->
            profileRepository.getById(Uuid.parseHex(profileId)).first()
        } ?: profileRepository
            .getAll().first()
            .filterIsInstance<Profile.StudentProfile>()
            .first { it.groupId == group.id }
        keyValueRepository.delete(Keys.VPP_ID_LOGIN_LINK_TO_PROFILE)
        profileRepository.updateVppId(profile.id, vppId.data.id)
        syncGradesUseCase(false)
        return Response.Success(vppIdRepository.getByLocalId(vppId.data.id).first() as VppId.Active)
    }
}