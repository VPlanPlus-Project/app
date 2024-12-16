package plus.vplan.app.domain.model

import kotlin.uuid.Uuid

data class EntityIdentifier(
    val entityId: Uuid,
    val origin: Origin,
    val value: String
) {
    enum class Origin {
        VPP, INDIWARE
    }
}