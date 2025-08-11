package plus.vplan.app.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

infix fun LocalDateTime.until(other: LocalDateTime): Duration {
    val start = this.toInstant(TimeZone.UTC)
    val end = other.toInstant(TimeZone.UTC)
    return start.until(end, DateTimeUnit.MILLISECOND).milliseconds
}

infix fun LocalDateTime.progressIn(range: ClosedRange<LocalDateTime>): Double {
    val start = range.start.toInstant(TimeZone.currentSystemDefault()).epochSeconds
    val end = range.endInclusive.toInstant(TimeZone.currentSystemDefault()).epochSeconds
    val current = this.toInstant(TimeZone.currentSystemDefault()).epochSeconds.toDouble()
    return (current - start) / (end - start)
}

operator fun LocalDateTime.plus(duration: Duration): LocalDateTime {
    val instant = toInstant(TimeZone.UTC)
    return instant.plus(duration).toLocalDateTime(TimeZone.UTC)
}

operator fun LocalDateTime.minus(duration: Duration): LocalDateTime {
    val instant = toInstant(TimeZone.UTC)
    return instant.minus(duration).toLocalDateTime(TimeZone.UTC)
}

fun LocalDateTime.Companion.now(): LocalDateTime {
    //return LocalDateTime(2025, 5, 27, 7, 0, 0)
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}