package plus.vplan.app.feature.settings.page.info.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.ProfileType
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase

class GetFeedbackMetadataUseCase(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase
) {
    suspend operator fun invoke(): Flow<FeedbackMetadata> {
        val systemInfo = getSystemInfo()
        val currentProfile = getCurrentProfileUseCase().map { currentProfile ->
            val school = currentProfile.getSchool().getFirstValue()!!
            return@map FeedbackMetadata(
                systemInfo,
                profileInfo = FeedbackProfileInfo(
                    schoolId = school.id,
                    school = school.name,
                    profileName = currentProfile.name,
                    profileType = currentProfile.profileType
                )
            )
        }
        return currentProfile
    }
}

data class FeedbackMetadata(
    val deviceInfo: FeedbackDeviceInfo,
    val profileInfo: FeedbackProfileInfo
) {
    override fun toString(): String {
        return """
            ## Device
            OS: ${deviceInfo.os} ${deviceInfo.osVersion}
            Manufacturer: ${deviceInfo.manufacturer}
            Device: ${deviceInfo.device}
            
            ## Profile
            School: ${profileInfo.schoolId} (${profileInfo.school})
            Profile: ${profileInfo.profileType.name} ${profileInfo.profileName}
        """.trimIndent()
    }
}

data class FeedbackDeviceInfo(
    val os: String,
    val osVersion: String,
    val manufacturer: String,
    val device: String,
    val deviceName: String = device
)

data class FeedbackProfileInfo(
    val schoolId: Int,
    val school: String,
    val profileType: ProfileType,
    val profileName: String
)

expect fun getSystemInfo(): FeedbackDeviceInfo