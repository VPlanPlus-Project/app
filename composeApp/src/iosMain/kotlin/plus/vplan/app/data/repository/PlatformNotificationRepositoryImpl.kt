package plus.vplan.app.data.repository

import co.touchlab.kermit.Logger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import kotlin.uuid.Uuid

class PlatformNotificationRepositoryImpl : PlatformNotificationRepository {
    override suspend fun initialize() {}

    override suspend fun sendNotification(
        title: String,
        message: String,
        category: String?,
        isLarge: Boolean,
        largeText: String?,
        onClickData: String?
    ) {
        val notification = UNMutableNotificationContent()
        notification.setTitle(title)
        category?.let { notification.setSubtitle(it) }
        notification.setBody(message)
        notification.setSound(UNNotificationSound.defaultSound)
        if (onClickData != null) notification.setUserInfo(mapOf("data" to onClickData))

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(1.0, false)
        val notificationRequest = UNNotificationRequest.requestWithIdentifier(
            identifier = Uuid.random().toHexString(),
            content = notification,
            trigger = trigger
        )
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(notificationRequest) { error ->
            error?.let { Logger.e { "Error showing notification: $error" } }
        }
    }
}