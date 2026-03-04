package plus.vplan.app.core.model

sealed class AppEntity {
    data class VppId(val vppId: plus.vplan.app.core.model.VppId) : AppEntity()
    data class Profile(val profile: plus.vplan.app.core.model.Profile) : AppEntity()
}