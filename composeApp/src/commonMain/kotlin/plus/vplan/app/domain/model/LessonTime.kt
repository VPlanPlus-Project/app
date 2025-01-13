package plus.vplan.app.domain.model

import kotlinx.datetime.LocalTime
import plus.vplan.app.domain.cache.Item

data class LessonTime(
    val id: String,
    val start: LocalTime,
    val end: LocalTime,
    val lessonNumber: Int,
    val group: Int,
    val interpolated: Boolean = false
) : Item {
    override fun getEntityId(): String = this.id
}
