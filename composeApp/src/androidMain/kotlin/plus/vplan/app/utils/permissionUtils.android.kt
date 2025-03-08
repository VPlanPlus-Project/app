package plus.vplan.app.utils

import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import network.chaintech.cmpeasypermission.PermissionState
import plus.vplan.app.activity

actual fun isPermissionGranted(permissionState: PermissionState): Boolean {
    return ContextCompat.checkSelfPermission(activity, permissionState.value) == PackageManager.PERMISSION_GRANTED
}