package plus.vplan.app.domain.model

import kotlin.uuid.Uuid

data class Room(
    val appId: Uuid,
    val identifiers: List<EntityIdentifier>,
    val school: School,
    val name: String
)
