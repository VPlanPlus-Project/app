package plus.vplan.app.domain.model

import plus.vplan.app.App
import kotlin.uuid.Uuid

sealed class AppEntity {
    data class VppId(val id: Int) : AppEntity() {
        var vppId: plus.vplan.app.domain.model.VppId? = null
            private set

        suspend fun getVppIdItem(): plus.vplan.app.domain.model.VppId? {
            return vppId ?: App.vppIdSource.getSingleById(id).also { vppId = it }
        }
    }
    data class Profile(val id: Uuid) : AppEntity() {
        var profile: plus.vplan.app.domain.model.Profile? = null
            private set

        suspend fun getProfileItem(): plus.vplan.app.domain.model.Profile? {
            return profile ?: App.profileSource.getSingleById(id).also { profile = it }
        }
    }
}