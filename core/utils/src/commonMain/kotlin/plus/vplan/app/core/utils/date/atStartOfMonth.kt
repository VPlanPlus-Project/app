package plus.vplan.app.core.utils.date

import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.days

fun LocalDate.atStartOfMonth(): LocalDate {
    return this - day.minus(1).days
}
