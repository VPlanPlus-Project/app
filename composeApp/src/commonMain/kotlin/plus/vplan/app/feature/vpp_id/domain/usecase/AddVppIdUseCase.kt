package plus.vplan.app.feature.vpp_id.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.captureError
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.data.school.SchoolRepository
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.getFirstValue
import plus.vplan.app.core.model.getFirstValueOld
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.VppDbDto
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.feature.sync.domain.usecase.besteschule.SyncGradesUseCase
import kotlin.uuid.Uuid

class AddVppIdUseCase(
    private val vppIdRepository: VppIdRepository,
    private val keyValueRepository: KeyValueRepository,
    private val profileRepository: ProfileRepository,
    private val syncGradesUseCase: SyncGradesUseCase,
    private val groupRepository: GroupRepository,
    private val schoolRepository: SchoolRepository,
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

        val vppIdDto = vppIdRepository.getUserByToken(accessToken.data)

        if (vppIdDto is Response.Error) return vppIdDto
        vppIdDto as Response.Success

        logger.i { "Resolved token to user ${vppIdDto.data.id} (${vppIdDto.data.username})" }

        schoolRepository.getById(Alias(AliasProvider.Vpp, vppIdDto.data.schoolId.toString(), 1))
            .first() ?: return Response.Error.Other("School not found for VPP ID: ${vppIdDto.data.id}")

        logger.d { "Loaded school by vpp school id ${vppIdDto.data.schoolId}" }

        val group = groupRepository.findByAlias(
            alias = Alias(AliasProvider.Vpp, vppIdDto.data.groupId.toString(), 1),
            forceUpdate = false,
            preferCurrentState = false
        ).getFirstValue()

        if (group == null) {
            val errorMessage = "Group not found for VPP ID: ${vppIdDto.data.id}, group alias: ${vppIdDto.data.groupId}"
            logger.e { errorMessage }
            captureError("AddVppIdUseCase", errorMessage)
            return Response.Error.Other("Group not found for VPP ID: ${vppIdDto.data.id}")
        }

        logger.d { "Assured a group with id ${vppIdDto.data.groupId} is cached on the app" }

        vppIdRepository.upsert(VppDbDto.AppVppDbDto(
            id = vppIdDto.data.id,
            username = vppIdDto.data.username,
            groups = listOf(group.aliases.first { it.provider == AliasProvider.Vpp }.value.toInt()),
            schulverwalterUserId = vppIdDto.data.schulverwalterId,
            schulverwalterAccessToken = vppIdDto.data.schulverwalterAccessToken,
            accessToken = accessToken.data
        ))

        val vppId = vppIdRepository.getById(vppIdDto.data.id, ResponsePreference.Fast)
            .getFirstValueOld() as? VppId.Active ?: throw RuntimeException("Vpp.ID not found")

        val profile = (keyValueRepository.get(Keys.VPP_ID_LOGIN_LINK_TO_PROFILE).first()?.let { profileId ->
            profileRepository.getById(Uuid.parseHex(profileId)).first() as Profile.StudentProfile
        } ?: profileRepository
            .getAll().first()
            .filterIsInstance<Profile.StudentProfile>()
            .first { it.group.id == group.id })
            .copy(
                vppId = vppId,
            )
        keyValueRepository.delete(Keys.VPP_ID_LOGIN_LINK_TO_PROFILE)
        profileRepository.save(profile)
        syncGradesUseCase(false)
        return Response.Success(vppId)
    }
}