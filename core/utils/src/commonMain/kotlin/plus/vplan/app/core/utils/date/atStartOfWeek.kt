package plus.vplan.app.core.utils.date

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus

fun LocalDate.atStartOfWeek(): LocalDate {
    return this.minus(this.dayOfWeek.isoDayNumber.minus(1), DateTimeUnit.DAY)
}