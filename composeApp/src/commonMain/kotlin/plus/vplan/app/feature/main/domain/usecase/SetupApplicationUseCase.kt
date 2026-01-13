package plus.vplan.app.feature.main.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import plus.vplan.app.captureError
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.feature.main.domain.usecase.setup.DoAssessmentsAndHomeworkIndexMigrationUseCase
import plus.vplan.app.feature.main.domain.usecase.setup.DownloadVppSchoolIdentifierUseCase
import plus.vplan.app.feature.main.domain.usecase.setup.RemoveDisconnectedVppIdsFromProfilesUseCase
import plus.vplan.app.feature.system.usecase.sp24.SendSp24CredentialsToServerUseCase
import plus.vplan.app.firebaseIdentify
import plus.vplan.app.isFeatureEnabled
import plus.vplan.app.posthogIdentify
import plus.vplan.app.setPostHogProperty
import plus.vplan.app.versionCode

class SetupApplicationUseCase(
    private val keyValueRepository: KeyValueRepository,
    private val sendSp24CredentialsToServerUseCase: SendSp24CredentialsToServerUseCase,
    private val doAssessmentsAndHomeworkIndexMigrationUseCase: DoAssessmentsAndHomeworkIndexMigrationUseCase,
    private val updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase,
    private val downloadVppSchoolIdentifierUseCase: DownloadVppSchoolIdentifierUseCase,
    private val removeDisconnectedVppIdsFromProfilesUseCase: RemoveDisconnectedVppIdsFromProfilesUseCase,
    private val profileRepository: ProfileRepository,
    private val vppIdRepository: VppIdRepository
) {
    private val logger = Logger.withTag("SetupApplicationUseCase")
    suspend operator fun invoke() {
        removeDisconnectedVppIdsFromProfilesUseCase()
        updateFirebaseTokenUseCase()
        if (keyValueRepository.get(Keys.PREVIOUS_APP_VERSION).first() != versionCode.toString()) {
            logger.i { "First run of VPlanPlus" }
            logger.d { "Saving migration flags" }

            keyValueRepository.set(Keys.MIGRATION_FLAG_ASSESSMENTS_HOMEWORK_INDICES, "true")
        }

        if (keyValueRepository.get(Keys.MIGRATION_FLAG_ASSESSMENTS_HOMEWORK_INDICES).first() != "true") {
            logger.i { "Migrating assessments and homework indices" }
            doAssessmentsAndHomeworkIndexMigrationUseCase()
        }

        keyValueRepository.set(Keys.PREVIOUS_APP_VERSION, versionCode.toString())
        if (isFeatureEnabled("core_download-vpp-school-identifier", true)) downloadVppSchoolIdentifierUseCase()

        if (isFeatureEnabled("core_analytics-identifier", true)) vppIdRepository.getAllLocalIds().first()
            .firstNotNullOfOrNull { vppIdRepository.getByLocalId(it).first() as? VppId.Active }
            ?.let { vppId ->
                setProperty("user.vpp_id", vppId.id.toString())
                setPostHogProperty("user.vpp_id", vppId.id.toString())
                posthogIdentify(
                    distinctId = "vpp.ID/${vppId.id}",
                    userProperties = mapOf(
                        "vpp.id" to vppId.id.toString(),
                        "vpp.name" to vppId.name,
                    ),
                    userPropertiesSetOnce = emptyMap()
                )
                firebaseIdentify("vpp.ID/${vppId.id}")
                logger.d { "Identified user for analytics with vpp.ID/${vppId.id}" }
            }

        if (isFeatureEnabled("core_anonymous-user-profiles", true)) try {
            val profiles = profileRepository.getAll().first().groupBy { it.getSchool().getFirstValue() }.mapNotNull { (school, profiles) ->
                if (school == null) return@mapNotNull null
                FirebaseUserProfiles(
                    school = school.name,
                    profiles = profiles.map { "${it.profileType}/${it.name}" }
                )
            }.let { Json.encodeToString(it) }
            setProperty("user.profiles", profiles)
            setPostHogProperty("user.profiles", profiles)
            logger.d { "Collected user profiles for analytics: $profiles" }
        } catch (e: Exception) {
            logger.e(e) { "Failed to collect user profiles for Firebase" }
            captureError("SetupApplicationUseCase", "Failed to collect user profiles for Firebase: ${e.stackTraceToString()}")
        }

        if (isFeatureEnabled("core_sp24-api-log", true)) sendSp24CredentialsToServerUseCase()
    }
}

@Serializable
private data class FirebaseUserProfiles(
    @SerialName("school") val school: String,
    @SerialName("profiles") val profiles: List<String>
)