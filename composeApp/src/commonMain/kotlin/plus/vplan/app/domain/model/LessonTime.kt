package plus.vplan.app.domain.model

import kotlinx.datetime.LocalTime

data class LessonTime(
    val id: String,
    val start: LocalTime,
    val end: LocalTime,
    val lessonNumber: Int,
    val group: Group,
    val interpolated: Boolean = false
)
