package plus.vplan.app.utils

import kotlinx.datetime.LocalTime
import plus.vplan.app.core.model.getFirstValueOld
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.domain.model.populated.PopulatedLesson

fun List<PopulatedLesson>.findCurrentLessons(atTime: LocalTime) = this
    .associateWith { it.lessonTime }
    .filterValues { it != null }
    .filter { (_, time) -> atTime in time!!.start..<time.end }
    .keys

fun List<PopulatedLesson>.getNextLessonStart(atTime: LocalTime) = this
    .associateWith { it.lessonTime }
    .filterValues { it != null }
    .filter { it.value!!.end >= atTime && atTime !in it.value!!.start..it.value!!.end }
    .minOfOrNull { it.value!!.start }