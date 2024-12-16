package plus.vplan.app.domain.model

import kotlin.uuid.Uuid

data class Group(
    val appId: Uuid,
    val identifiers: List<EntityIdentifier>,
    val school: School,
    val name: String
)