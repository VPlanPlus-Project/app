package plus.vplan.app.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

fun LocalDate.atStartOfWeek(): LocalDate {
    return this.minus(this.dayOfWeek.isoDayNumber.minus(1), DateTimeUnit.DAY)
}

infix operator fun LocalDate.plus(duration: Duration): LocalDate {
    return this.plus(duration.inWholeDays, DateTimeUnit.DAY)
}

infix operator fun LocalDate.minus(duration: Duration): LocalDate {
    return this.minus(duration.inWholeDays, DateTimeUnit.DAY)
}

infix fun LocalDateTime.progressIn(range: ClosedRange<LocalDateTime>): Double {
    val start = range.start.toInstant(TimeZone.currentSystemDefault()).epochSeconds
    val end = range.endInclusive.toInstant(TimeZone.currentSystemDefault()).epochSeconds
    val current = this.toInstant(TimeZone.currentSystemDefault()).epochSeconds.toDouble()
    return (current - start) / (end - start)
}

fun LocalDate.Companion.now(): LocalDate {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}

fun LocalDate.atStartOfMonth(): LocalDate {
    return this - this.dayOfMonth.minus(1).days
}