package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDate

/**
 * @param id The school id and the week number concatenated with a "/", e.g. "67/13" (13th week of current school year)
 */
data class Week(
    val id: String,
    val calendarWeek: Int,
    val start: LocalDate,
    val end: LocalDate,
    val weekType: String,
    val weekIndex: Int,
    val school: School
) {
    constructor(
        calendarWeek: Int,
        start: LocalDate,
        end: LocalDate,
        weekType: String,
        weekIndex: Int,
        school: School
    ) : this(
        id = school.id.toString() + "/" + calendarWeek.toString(),
        calendarWeek = calendarWeek,
        start = start,
        end = end,
        weekType = weekType,
        weekIndex = weekIndex,
        school = school
    )
}