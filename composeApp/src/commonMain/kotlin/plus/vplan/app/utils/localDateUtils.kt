package plus.vplan.app.utils

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.time.Duration

fun LocalDate.atStartOfWeek(): LocalDate {
    return this.minus(this.dayOfWeek.isoDayNumber.minus(1), DateTimeUnit.DAY)
}

infix operator fun LocalDate.plus(duration: Duration): LocalDate {
    return this.plus(duration.inWholeDays, DateTimeUnit.DAY)
}