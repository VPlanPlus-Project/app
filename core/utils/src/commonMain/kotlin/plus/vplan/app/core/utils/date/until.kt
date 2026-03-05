package plus.vplan.app.core.utils.date

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.toInstant
import kotlinx.datetime.until
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

infix fun LocalDateTime.until(other: LocalDateTime): Duration {
    val start = this.toInstant(TimeZone.UTC)
    val end = other.toInstant(TimeZone.UTC)
    return start.until(end, DateTimeUnit.MILLISECOND).milliseconds
}

infix fun LocalTime.until(other: LocalTime): Duration {
    val start = this.atDate(LocalDate.fromEpochDays(0)).toInstant(TimeZone.UTC)
    val end = other.atDate(LocalDate.fromEpochDays(0)).toInstant(TimeZone.UTC)
    return start.until(end, DateTimeUnit.MILLISECOND).milliseconds
}
