package plus.vplan.app.feature.system.usecase.sp24.check_sp24_credentials_validity

import co.touchlab.kermit.Logger
import kotlinx.serialization.json.Json
import plus.vplan.app.StartTaskJson
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import kotlin.uuid.Uuid

class SendInvalidSp24CredentialsNotification(
    private val platformNotificationRepository: PlatformNotificationRepository
) {
    suspend operator fun invoke(
        schoolName: String,
        schoolId: Uuid
    ) {
        platformNotificationRepository.sendNotification(
            title = "Schulzugangsdaten abgelaufen",
            message = "Die Schulzugangsdaten für $schoolName sind abgelaufen. Tippe, um sie zu aktualisieren.",
            category = schoolName,
            isLarge = false,
            onClickData = Json.encodeToString(
                StartTaskJson(
                    type = "navigate_to",
                    value = Json.encodeToString(
                        StartTaskJson.StartTaskNavigateTo(
                            screen = "settings/school",
                            value = Json.encodeToString(
                                StartTaskJson.StartTaskNavigateTo.SchoolSettings(
                                    openIndiwareSettingsSchoolId = schoolId
                                )
                            )
                        )
                    )
                )
            ).also { Logger.d { "Task: $it" } }
        )
    }
}