package plus.vplan.app.core.utils.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime

fun LocalDate.atStartOfDay(): LocalDateTime {
    return this.atTime(LocalTime.fromSecondOfDay(0))
}
