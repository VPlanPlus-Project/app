@file:OptIn(kotlin.time.ExperimentalTime::class)

package plus.vplan.app.feature.vpp_id.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.captureError
import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.core.data.group.GroupRepository
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.data.school.SchoolRepository
import plus.vplan.app.core.data.vpp_id.VppIdRepository
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.NetworkErrorKind
import plus.vplan.app.core.model.NetworkException
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.VppId
import plus.vplan.app.feature.sync.domain.usecase.besteschule.SyncGradesUseCase
import kotlin.time.Clock
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

    suspend operator fun invoke(token: String): VppId.Active {
        val accessToken = vppIdRepository.getAccessToken(token)

        logger.i { "Got access token" }

        val vppIdInfo = vppIdRepository.getUserByToken(accessToken)

        logger.i { "Resolved token to user ${vppIdInfo.id} (${vppIdInfo.username})" }

        schoolRepository.getById(Alias(AliasProvider.Vpp, vppIdInfo.schoolId.toString(), 1))
            .first() ?: throw NetworkException(NetworkErrorKind.NotFound, "School not found for VPP ID: ${vppIdInfo.id}")

        logger.d { "Loaded school by vpp school id ${vppIdInfo.schoolId}" }

        val group = groupRepository.getById(
            identifier = Alias(AliasProvider.Vpp, vppIdInfo.groupId.toString(), 1),
            forceUpdate = false,
        ).first()

        if (group == null) {
            val errorMessage = "Group not found for VPP ID: ${vppIdInfo.id}, group alias: ${vppIdInfo.groupId}"
            logger.e { errorMessage }
            captureError("AddVppIdUseCase", errorMessage)
            throw NetworkException(NetworkErrorKind.NotFound, "Group not found for VPP ID: ${vppIdInfo.id}")
        }

        logger.d { "Assured a group with id ${vppIdInfo.groupId} is cached on the app" }

        vppIdRepository.save(VppId.Active(
            id = vppIdInfo.id,
            name = vppIdInfo.username,
            groups = listOf(group.aliases.first { it.provider == AliasProvider.Vpp }.value.toInt()),
            cachedAt = Clock.System.now(),
            schulverwalterConnection = run {
                val svId = vppIdInfo.schulverwalterId
                val svToken = vppIdInfo.schulverwalterAccessToken
                if (svId != null && svToken != null) VppId.Active.SchulverwalterConnection(
                    accessToken = svToken,
                    userId = svId,
                    isValid = null
                ) else null
            },
            accessToken = accessToken
        ))

        val vppId = vppIdRepository.getById(vppIdInfo.id).first() as? VppId.Active
            ?: throw RuntimeException("Vpp.ID not found after save")

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
        return vppId
    }
}
