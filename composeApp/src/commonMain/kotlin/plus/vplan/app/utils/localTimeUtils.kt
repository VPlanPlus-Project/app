@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.utils

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

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

operator fun LocalTime.minus(other: LocalTime): LocalTime {
    return this.minus(other.toSecondOfDay().seconds)
}

fun LocalTime.minusWithCapAtMidnight(duration: Duration): LocalTime {
    val seconds = this.toSecondOfDay()
    val newSeconds = (seconds - duration.inWholeSeconds).coerceAtLeast(0)
    return LocalTime.fromSecondOfDay(newSeconds.toInt())
}

fun LocalTime.plusWithCapAtMidnight(duration: Duration): LocalTime {
    val seconds = this.toSecondOfDay()
    val newSeconds = (seconds + duration.inWholeSeconds).coerceAtMost(24 * 60 * 60)
    return LocalTime.fromSecondOfDay(newSeconds.toInt())
}

/**
 * Returns the total number of minutes represented by this LocalTime.
 */
fun LocalTime.inWholeMinutes(): Int {
    return this.toSecondOfDay() / 60
}

infix fun LocalTime.progressIn(range: ClosedRange<LocalTime>): Double {
    val start = range.start.toSecondOfDay()
    val end = range.endInclusive.toSecondOfDay()
    val current = this.toSecondOfDay().toDouble()
    return (current - start) / (end - start)
}

val regularTimeFormat = LocalTime.Format {
    hour(Padding.ZERO)
    char(':')
    minute(Padding.ZERO)
}

fun LocalTime.Companion.now(): LocalTime {
    //return LocalTime(7, 0)
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
}
