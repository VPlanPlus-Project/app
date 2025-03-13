package plus.vplan.app.utils

import kotlinx.datetime.LocalTime
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Lesson

suspend fun List<Lesson>.findCurrentLessons(atTime: LocalTime) = this.associateWith { it.lessonTime.getFirstValue()!! }.filter { (_, time) ->
    atTime in time.start..<time.end
}.keys

suspend fun List<Lesson>.getLastLessonEnd() = this.associateWith { it.lessonTime.getFirstValue()!! }.maxOf { it.value.end }

suspend fun List<Lesson>.getNextLessonStart(atTime: LocalTime) = this
    .associateWith { it.lessonTime.getFirstValue()!! }
    .filter { it.value.end >= atTime && atTime !in it.value.start..it.value.end }
    .minOfOrNull { it.value.start }