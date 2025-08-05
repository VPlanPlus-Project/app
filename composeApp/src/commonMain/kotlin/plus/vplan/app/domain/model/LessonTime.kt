package plus.vplan.app.domain.model

import kotlinx.datetime.LocalTime
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.data.Item
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
}
