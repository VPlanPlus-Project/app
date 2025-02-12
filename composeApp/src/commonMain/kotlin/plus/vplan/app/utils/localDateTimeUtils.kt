package plus.vplan.app.utils

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.until
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

infix fun LocalDateTime.until(other: LocalDateTime): Duration {
    val start = this.toInstant(TimeZone.UTC)
    val end = other.toInstant(TimeZone.UTC)
    return start.until(end, DateTimeUnit.MILLISECOND).milliseconds
}