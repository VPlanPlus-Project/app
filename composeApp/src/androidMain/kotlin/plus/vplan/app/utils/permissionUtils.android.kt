package plus.vplan.app.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import network.chaintech.cmpeasypermission.PermissionState
import org.koin.core.context.GlobalContext

actual fun isPermissionGranted(permissionState: PermissionState): Boolean {
    val context = GlobalContext.get().get<Context>()
    return ContextCompat.checkSelfPermission(context, permissionState.value) == PackageManager.PERMISSION_GRANTED
}