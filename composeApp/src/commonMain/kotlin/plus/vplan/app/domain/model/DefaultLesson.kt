package plus.vplan.app.domain.model

import kotlin.uuid.Uuid

data class DefaultLesson(
    val appId: Uuid,
    val identifier: List<EntityIdentifier>,
    val subject: String,
    val course: Course?,
    val teacher: Teacher?,
    val group: Group
)