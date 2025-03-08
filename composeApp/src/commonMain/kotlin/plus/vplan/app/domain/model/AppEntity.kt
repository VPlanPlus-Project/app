package plus.vplan.app.domain.model

import plus.vplan.app.App
import kotlin.uuid.Uuid

sealed class AppEntity {
    data class VppId(val id: Int) : AppEntity() {
        val vppId by lazy { App.vppIdSource.getById(id) }
    }
    data class Profile(val id: Uuid) : AppEntity() {
        val profile by lazy { App.profileSource.getById(id) }
    }
}