package plus.vplan.app.utils

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.until
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

infix operator fun LocalDate.plus(duration: Duration): LocalDate {
    return this.plus(duration.inWholeDays, DateTimeUnit.DAY)
}

infix operator fun LocalDate.minus(duration: Duration): LocalDate {
    return this.minus(duration.inWholeDays, DateTimeUnit.DAY)
}

fun LocalDate.atStartOfMonth(): LocalDate {
    return this - day.minus(1).days
}

infix fun LocalDate.untilText(other: LocalDate): String {
    when (val days = this.until(other, DateTimeUnit.DAY)) {
        -2L -> return "Vorgestern"
        -1L -> return "Gestern"
        0L -> return "Heute"
        1L -> return "Morgen"
        2L -> return "Übermorgen"
        else -> {
            if (days > 0) return "In $days Tagen"
            return "Vor ${abs(days)} Tagen"
        }
    }
}

fun LocalDate.atStartOfDay(): LocalDateTime {
    return this.atTime(LocalTime.fromSecondOfDay(0))
}
