package plus.vplan.app.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import kotlin.math.abs
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

infix fun LocalDate.progressIn(range: ClosedRange<LocalDate>): Double {
    val start = range.start.atStartOfDayIn(TimeZone.currentSystemDefault()).epochSeconds
    val end = range.endInclusive.atStartOfDayIn(TimeZone.currentSystemDefault()).epochSeconds
    val current = this.atStartOfDayIn(TimeZone.currentSystemDefault()).epochSeconds.toDouble()
    return (current - start) / (end - start)
}

fun LocalDate.Companion.now(): LocalDate {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}

fun LocalDate.atStartOfMonth(): LocalDate {
    return this - this.dayOfMonth.minus(1).days
}

infix fun LocalDate.untilText(other: LocalDate): String {
    when (val days = this.until(other, DateTimeUnit.DAY)) {
        -2 -> return "Vorgestern"
        -1 -> return "Gestern"
        0 -> return "Heute"
        1 -> return "Morgen"
        2 -> return "Übermorgen"
        else -> {
            if (days > 0) return "In $days Tagen"
            return "Vor ${abs(days)} Tagen"
        }
    }
}

infix fun LocalDate.untilRelativeText(other: LocalDate): String? {
    val days = (other - this).days
    return when (days) {
        -2 -> "Vorgestern"
        -1 -> "Gestern"
        0 -> "Heute"
        1 -> "Morgen"
        2 -> "Übermorgen"
        else -> null
    }
}

fun LocalDate.atStartOfDay(): LocalDateTime {
    return this.atTime(LocalTime.fromSecondOfDay(0))
}

val regularDateFormat = LocalDate.Format {
    dayOfMonth(Padding.ZERO)
    char('.')
    monthNumber(Padding.ZERO)
    char('.')
    year(Padding.ZERO)
}

val dateFormatDDMMMYY = LocalDate.Format {
    dayOfMonth(Padding.ZERO)
    chars(". ")
    monthName(shortMonthNames)
    char(' ')
    yearTwoDigits(2000)
}

val regularDateFormatWithoutYear = LocalDate.Format {
    dayOfMonth(Padding.ZERO)
    char('.')
    monthNumber(Padding.ZERO)
}