package plus.vplan.app.core.utils.date

import kotlinx.datetime.LocalTime

/**
 * Returns the total number of minutes represented by this LocalTime.
 */
fun LocalTime.inWholeMinutes(): Int {
    return this.toSecondOfDay() / 60
}