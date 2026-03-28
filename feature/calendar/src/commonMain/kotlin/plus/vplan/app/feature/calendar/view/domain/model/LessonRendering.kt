package plus.vplan.app.feature.calendar.view.domain.model

import plus.vplan.app.core.model.Lesson

sealed class LessonRendering {
    data class ListView(val lessons: Map<Int, List<Lesson>>) : LessonRendering()
    data class Layouted(val lessons: List<LessonLayoutingInfo>) : LessonRendering()

    val size: Int
        get() = when (this) {
            is ListView -> lessons.size
            is Layouted -> lessons.size
        }
}