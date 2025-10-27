package plus.vplan.app.utils

import kotlinx.datetime.LocalTime
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.Lesson

suspend fun List<Lesson>.findCurrentLessons(atTime: LocalTime) = this
    .associateWith { it.lessonTime?.getFirstValueOld() }
    .filterValues { it != null }
    .filter { (_, time) -> atTime in time!!.start..<time.end }
    .keys

suspend fun List<Lesson>.getNextLessonStart(atTime: LocalTime) = this
    .associateWith { it.lessonTime?.getFirstValueOld() }
    .filterValues { it != null }
    .filter { it.value!!.end >= atTime && atTime !in it.value!!.start..it.value!!.end }
    .minOfOrNull { it.value!!.start }