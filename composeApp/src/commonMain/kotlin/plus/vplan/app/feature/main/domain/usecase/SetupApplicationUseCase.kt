package plus.vplan.app.feature.main.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import plus.vplan.app.AppBuildConfig
import plus.vplan.app.core.analytics.AnalyticsRepository
import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.data.school.SchoolRepository
import plus.vplan.app.core.data.vpp_id.VppIdRepository
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.VppId
import plus.vplan.app.feature.main.domain.usecase.setup.DoAssessmentsAndHomeworkIndexMigrationUseCase
import plus.vplan.app.feature.main.domain.usecase.setup.DownloadVppSchoolIdentifierUseCase
import plus.vplan.app.feature.system.usecase.sp24.SendSp24CredentialsToServerUseCase

class SetupApplicationUseCase(
    private val keyValueRepository: KeyValueRepository,
    private val sendSp24CredentialsToServerUseCase: SendSp24CredentialsToServerUseCase,
    private val doAssessmentsAndHomeworkIndexMigrationUseCase: DoAssessmentsAndHomeworkIndexMigrationUseCase,
    private val updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase,
    private val downloadVppSchoolIdentifierUseCase: DownloadVppSchoolIdentifierUseCase,
    private val profileRepository: ProfileRepository,
    private val vppIdRepository: VppIdRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val schoolRepository: SchoolRepository,
) {
    private val logger = Logger.withTag("SetupApplicationUseCase")
    suspend operator fun invoke() {
        updateFirebaseTokenUseCase()
        if (keyValueRepository.get(Keys.PREVIOUS_APP_VERSION).first() != AppBuildConfig.APP_VERSION_CODE.toString()) {
            logger.i { "First run of VPlanPlus" }
            logger.d { "Saving migration flags" }

            keyValueRepository.set(Keys.MIGRATION_FLAG_ASSESSMENTS_HOMEWORK_INDICES, "true")
        }

        if (keyValueRepository.get(Keys.MIGRATION_FLAG_ASSESSMENTS_HOMEWORK_INDICES).first() != "true") {
            logger.i { "Migrating assessments and homework indices" }
            doAssessmentsAndHomeworkIndexMigrationUseCase()
        }

        keyValueRepository.set(Keys.PREVIOUS_APP_VERSION, AppBuildConfig.APP_VERSION_CODE.toString())
        if (analyticsRepository.isFeatureEnabled("core_download-vpp-school-identifier", true)) downloadVppSchoolIdentifierUseCase()

        if (analyticsRepository.isFeatureEnabled("core_analytics-identifier", true)) vppIdRepository.getAllLocalIds().first()
            .firstNotNullOfOrNull { vppIdRepository.getById(it).first() as? VppId.Active }
            ?.let { vppId ->
                setProperty("user.vpp_id", vppId.id.toString())
                analyticsRepository.setPostHogProperty("user.vpp_id", vppId.id.toString())
                analyticsRepository.posthogIdentify(
                    distinctId = "vpp.ID/${vppId.id}",
                    userProperties = mapOf(
                        "vpp.id" to vppId.id.toString(),
                        "vpp.name" to vppId.name,
                    ),
                    userPropertiesSetOnce = emptyMap()
                )
                analyticsRepository.firebaseIdentify("vpp.ID/${vppId.id}")
                logger.d { "Identified user for analytics with vpp.ID/${vppId.id}" }
            }

        if (analyticsRepository.isFeatureEnabled("core_anonymous-user-profiles", true)) try {
            val profiles = profileRepository.getAll().first().groupBy { it.school }.mapNotNull { (school, profiles) ->
                FirebaseUserProfiles(
                    school = school.name,
                    profiles = profiles.map { "${it.profileType}/${it.name}" }
                )
            }.let { Json.encodeToString(it) }
            setProperty("user.profiles", profiles)
            analyticsRepository.setPostHogProperty("user.profiles", profiles)
            logger.d { "Collected user profiles for analytics: $profiles" }
        } catch (e: Exception) {
            logger.e(e) { "Failed to collect user profiles for Firebase" }
            analyticsRepository.captureError("SetupApplicationUseCase", "Failed to collect user profiles for Firebase: ${e.stackTraceToString()}")
        }

        if (analyticsRepository.isFeatureEnabled("core_sp24-api-log", true)) sendSp24CredentialsToServerUseCase()

        profileRepository.getAll().first()
            .map { it.school }
            .distinctBy { it.id }
            .filter { it.aliases.none { alias -> alias.provider == AliasProvider.Vpp } }
            .forEach { school ->
                schoolRepository.getById(school.aliases.first(), forceReload = true).first()
            }
    }
}

@Serializable
private data class FirebaseUserProfiles(
    @SerialName("school") val school: String,
    @SerialName("profiles") val profiles: List<String>
)