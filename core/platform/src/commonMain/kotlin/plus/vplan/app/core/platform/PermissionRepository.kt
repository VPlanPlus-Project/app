package plus.vplan.app.core.platform

import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController

interface PermissionRepository {
    val controller: PermissionsController

    suspend fun isGranted(permission: Permission): Boolean
    suspend fun request(permission: Permission)
}
