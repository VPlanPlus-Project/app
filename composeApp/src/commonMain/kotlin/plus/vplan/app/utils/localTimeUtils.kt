package plus.vplan.app.utils

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

infix fun LocalTime.until(other: LocalTime): Duration {
    val start = this.atDate(LocalDate.fromEpochDays(0)).toInstant(TimeZone.UTC)
    val end = other.atDate(LocalDate.fromEpochDays(0)).toInstant(TimeZone.UTC)
    return start.until(end, DateTimeUnit.MILLISECOND).milliseconds
}

operator fun LocalTime.plus(duration: Duration): LocalTime {
    val instant = atDate(LocalDate.fromEpochDays(0)).toInstant(TimeZone.UTC)
    return instant.plus(duration).toLocalDateTime(TimeZone.UTC).time
}

operator fun LocalTime.minus(duration: Duration): LocalTime {
    val instant = atDate(LocalDate.fromEpochDays(0)).toInstant(TimeZone.UTC)
    return instant.minus(duration).toLocalDateTime(TimeZone.UTC).time
}

fun LocalTime.inWholeMinutes(): Int {
    return this.toSecondOfDay() / 60
}

infix fun LocalTime.progressIn(range: ClosedRange<LocalTime>): Double {
    val start = range.start.toSecondOfDay()
    val end = range.endInclusive.toSecondOfDay()
    val current = this.toSecondOfDay().toDouble()
    return (current - start) / (end - start)
}