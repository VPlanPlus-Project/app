package plus.vplan.app.core.platform

import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController

class PermissionRepositoryImpl(
    override val controller: PermissionsController
) : PermissionRepository {

    override suspend fun isGranted(permission: Permission): Boolean =
        controller.isPermissionGranted(permission)

    override suspend fun request(permission: Permission) =
        controller.providePermission(permission)
}
