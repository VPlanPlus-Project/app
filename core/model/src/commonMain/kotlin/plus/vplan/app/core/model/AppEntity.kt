package plus.vplan.app.core.model

import kotlin.uuid.Uuid

sealed class AppEntity {
    data class VppId(val id: Int) : AppEntity()
    data class Profile(val id: Uuid) : AppEntity()
}