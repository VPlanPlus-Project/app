package plus.vplan.app.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


operator fun LocalTime.minus(duration: Duration): LocalTime {
    val instant = atDate(LocalDate.fromEpochDays(0)).toInstant(TimeZone.UTC)
    return instant.minus(duration).toLocalDateTime(TimeZone.UTC).time
}

operator fun LocalTime.minus(other: LocalTime): LocalTime {
    return this.minus(other.toSecondOfDay().seconds)
}

infix fun LocalTime.progressIn(range: ClosedRange<LocalTime>): Double {
    val start = range.start.toSecondOfDay()
    val end = range.endInclusive.toSecondOfDay()
    val current = this.toSecondOfDay().toDouble()
    return (current - start) / (end - start)
}