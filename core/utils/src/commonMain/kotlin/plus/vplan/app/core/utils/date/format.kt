package plus.vplan.app.core.utils.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
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

val regularDateTimeFormat = LocalDateTime.Format {
    day(padding = Padding.ZERO)
    char('.')
    monthNumber(Padding.ZERO)
    chars(", ")
    hour(Padding.ZERO)
    char(':')
    minute(Padding.ZERO)
}

val regularTimeFormat = LocalTime.Format {
    hour(Padding.ZERO)
    char(':')
    minute(Padding.ZERO)
}