package plus.vplan.app.feature.system.usecase.sp24.check_sp24_credentials_validity

import co.touchlab.kermit.Logger
import kotlinx.serialization.json.Json
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.application.StartTaskJson
import plus.vplan.app.core.platform.NotificationRepository

class SendInvalidSp24CredentialsNotification(
    private val platformNotificationRepository: NotificationRepository
) {
    suspend operator fun invoke(
        schoolName: String,
        school: Alias
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
                                    openIndiwareSettingsSchool = school
                                )
                            )
                        )
                    )
                )
            ).also { Logger.d { "Task: $it" } }
        )
    }
}