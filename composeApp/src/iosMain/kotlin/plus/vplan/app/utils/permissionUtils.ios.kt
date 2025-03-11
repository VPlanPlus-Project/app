package plus.vplan.app.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import network.chaintech.cmpeasypermission.PermissionState
import plus.vplan.app.NotificationPermissionHelper

actual fun isPermissionGranted(permissionState: PermissionState): Boolean {
    return runBlocking {
        var result: Boolean? = null
        NotificationPermissionHelper.checkNotificationPermission {
            result = it
        }
        while (result == null) delay(10)
        return@runBlocking result!!
    }
}