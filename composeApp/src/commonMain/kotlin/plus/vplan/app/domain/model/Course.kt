package plus.vplan.app.domain.model

import kotlin.uuid.Uuid

data class Course(
    val appId: Uuid,
    val identifiers: List<EntityIdentifier>,
    val group: Group,
    val name: String,
    val teacher: Teacher,
)
