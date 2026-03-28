package plus.vplan.app.feature.calendar.view.domain.model

import plus.vplan.app.core.model.Lesson

data class LessonLayoutingInfo(
    val lesson: Lesson,
    val sideShift: Int,
    val of: Int
)