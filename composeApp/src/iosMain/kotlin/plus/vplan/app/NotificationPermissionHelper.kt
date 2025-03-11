package plus.vplan.app

import platform.UserNotifications.*

object NotificationPermissionHelper {

    fun checkNotificationPermission(callback: (Boolean) -> Unit) {
        UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
            callback(settings?.authorizationStatus == UNAuthorizationStatusAuthorized)
        }
    }
}
