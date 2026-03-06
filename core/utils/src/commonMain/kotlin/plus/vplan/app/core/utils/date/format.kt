package plus.vplan.app.core.utils.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

val regularDateFormat = LocalDate.Format {
    day(padding = Padding.ZERO)
    char('.')
    monthNumber(Padding.ZERO)
    char('.')
    year(Padding.ZERO)
}

val dateFormatDDMMMYY = LocalDate.Format {
    day(padding = Padding.ZERO)
    chars(". ")
    monthName(shortMonthNames)
    char(' ')
    yearTwoDigits(2000)
}

val regularDateFormatWithoutYear = LocalDate.Format {
    day(padding = Padding.ZERO)
    char('.')
    monthNumber(Padding.ZERO)
}