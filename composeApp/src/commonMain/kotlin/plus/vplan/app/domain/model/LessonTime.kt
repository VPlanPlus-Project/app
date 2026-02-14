package plus.vplan.app.domain.model

import kotlinx.datetime.LocalTime
import plus.vplan.app.core.model.DataTag
import plus.vplan.app.core.model.Item
import kotlin.uuid.Uuid

data class LessonTime(
    override val id: String,
    val start: LocalTime,
    val end: LocalTime,
    val lessonNumber: Int,
    val group: Uuid,
    val interpolated: Boolean = false
) : Item<String, DataTag> {
    override val tags: Set<DataTag> = emptySet()

    companion object {
        fun buildId(schoolId: Uuid, groupId: Uuid, lessonNumber: Int) = "${schoolId}/${groupId}/${lessonNumber}"
    }
}
