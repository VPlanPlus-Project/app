package plus.vplan.app.utils

import kotlinx.datetime.LocalTime

infix fun LocalTime.progressIn(range: ClosedRange<LocalTime>): Double {
    val start = range.start.toSecondOfDay()
    val end = range.endInclusive.toSecondOfDay()
    val current = this.toSecondOfDay().toDouble()
    return (current - start) / (end - start)
}