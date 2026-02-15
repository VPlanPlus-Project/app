package plus.vplan.app.feature.vpp_id.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.captureError
import plus.vplan.app.core.model.getFirstValue
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.VppId
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.VppDbDto
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.service.SchoolService
import plus.vplan.app.feature.sync.domain.usecase.besteschule.SyncGradesUseCase
import kotlin.uuid.Uuid

class AddVppIdUseCase(
    private val vppIdRepository: VppIdRepository,
    private val keyValueRepository: KeyValueRepository,
    private val profileRepository: ProfileRepository,
    private val syncGradesUseCase: SyncGradesUseCase,
    private val groupRepository: GroupRepository,
    private val schoolService: SchoolService,
) {
    private val logger = Logger.withTag("AddVppIdUseCase")

    suspend operator fun invoke(token: String): Response<VppId.Active> {
        val accessToken = vppIdRepository.getAccessToken(token)
        if (accessToken is Response.Error) {
            logger.e { "Error getting access token: $accessToken" }
            return accessToken
        }
        if (accessToken !is Response.Success) throw IllegalStateException("Unexpected response type")

        logger.i { "Got access token" }

        val vppId = vppIdRepository.getUserByToken(accessToken.data)

        if (vppId is Response.Error) return vppId
        vppId as Response.Success

        logger.i { "Resolved token to user ${vppId.data.id} (${vppId.data.username})" }

        schoolService.getSchoolFromAlias(Alias(AliasProvider.Vpp, vppId.data.schoolId.toString(), 1)).getFirstValue()
            ?: return Response.Error.Other("School not found for VPP ID: ${vppId.data.id}")

        logger.d { "Loaded school by vpp school id ${vppId.data.schoolId}" }

        val group = groupRepository.findByAlias(
            alias = Alias(AliasProvider.Vpp, vppId.data.groupId.toString(), 1),
            forceUpdate = false,
            preferCurrentState = false
        ).getFirstValue()

        if (group == null) {
            val errorMessage = "Group not found for VPP ID: ${vppId.data.id}, group alias: ${vppId.data.groupId}"
            logger.e { errorMessage }
            captureError("AddVppIdUseCase", errorMessage)
            return Response.Error.Other("Group not found for VPP ID: ${vppId.data.id}")
        }

        logger.d { "Assured a group with id ${vppId.data.groupId} is cached on the app" }

        vppIdRepository.upsert(VppDbDto.AppVppDbDto(
            id = vppId.data.id,
            username = vppId.data.username,
            groups = listOf(group.aliases.first { it.provider == AliasProvider.Vpp }.value.toInt()),
            schulverwalterUserId = vppId.data.schulverwalterId,
            schulverwalterAccessToken = vppId.data.schulverwalterAccessToken,
            accessToken = accessToken.data
        ))

        val profile = keyValueRepository.get(Keys.VPP_ID_LOGIN_LINK_TO_PROFILE).first()?.let { profileId ->
            profileRepository.getById(Uuid.parseHex(profileId)).first()
        } ?: profileRepository
            .getAll().first()
            .filterIsInstance<Profile.StudentProfile>()
            .first { it.group.id == group.id }
        keyValueRepository.delete(Keys.VPP_ID_LOGIN_LINK_TO_PROFILE)
        profileRepository.updateVppId(profile.id, vppId.data.id)
        syncGradesUseCase(false)
        return Response.Success(vppIdRepository.getByLocalId(vppId.data.id).first() as VppId.Active)
    }
}