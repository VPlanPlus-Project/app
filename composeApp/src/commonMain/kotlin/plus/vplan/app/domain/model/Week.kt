package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.cache.Item

/**
 * @param getEntityId The school id and the week number concatenated with a "/", e.g. "67/13" (13th week of current school year)
 */
data class Week(
    val id: String,
    val calendarWeek: Int,
    val start: LocalDate,
    val end: LocalDate,
    val weekType: String,
    val weekIndex: Int,
    val school: Int
): Item {
    constructor(
        calendarWeek: Int,
        start: LocalDate,
        end: LocalDate,
        weekType: String,
        weekIndex: Int,
        school: Int
    ) : this(
        id = "$school/$calendarWeek",
        calendarWeek = calendarWeek,
        start = start,
        end = end,
        weekType = weekType,
        weekIndex = weekIndex,
        school = school
    )

    override fun getEntityId(): String = this.id
}