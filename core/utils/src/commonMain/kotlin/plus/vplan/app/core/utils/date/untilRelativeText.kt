package plus.vplan.app.core.utils.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

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