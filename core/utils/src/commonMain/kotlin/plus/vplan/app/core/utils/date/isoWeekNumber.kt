package plus.vplan.app.core.utils.date

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.isoDayNumber

fun LocalDate.isoWeekNumber(): Int {
    val year = this.year
    val month = this.month
    val day = this.dayOfMonth

    // Calculate the ordinal day of the year (1-based)
    val ordinalDay = this.dayOfYear

    // Calculate the weekday (Monday = 1, ..., Sunday = 7)
    val weekday = this.dayOfWeek.isoDayNumber

    // Calculate the ordinal day of the week (1-based, Monday = 1)
    val ordinalWeekday = (weekday + 5) % 7 + 1

    // Calculate the ordinal week number
    val ordinalWeek = (ordinalDay - ordinalWeekday + 10) / 7

    // Handle edge cases for weeks belonging to the previous or next year
    if (ordinalWeek < 1) {
        // Week belongs to the previous year
        val prevYearLastWeek = LocalDate(year - 1, Month.DECEMBER, 31).isoWeekNumber()
        return prevYearLastWeek
    } else if (ordinalWeek > 52 && LocalDate(year, Month.DECEMBER, 31).dayOfWeek < DayOfWeek.THURSDAY) {
        // Week belongs to the next year
        return 1
    } else {
        return ordinalWeek
    }
}