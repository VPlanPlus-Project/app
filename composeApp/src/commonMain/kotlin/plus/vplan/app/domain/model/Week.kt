package plus.vplan.app.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.data.Item
import plus.vplan.app.utils.atStartOfWeek
import kotlin.uuid.Uuid

/**
 * @param id The school id and the week number concatenated with a "/", e.g. "67/13" (13th week of current school year)
 */
data class Week(
    override val id: String,
    val calendarWeek: Int,
    val start: LocalDate,
    val end: LocalDate,
    val weekType: String,
    val weekIndex: Int,
    val school: Uuid
): Item<String, DataTag> {
    constructor(
        calendarWeek: Int,
        start: LocalDate,
        end: LocalDate,
        weekType: String,
        weekIndex: Int,
        school: Uuid
    ) : this(
        id = "$school/${if (end.atStartOfWeek().plus(DatePeriod(days = 3)).year != end.year) start.year else end.year}:KW$calendarWeek",
        calendarWeek = calendarWeek,
        start = start,
        end = end,
        weekType = weekType,
        weekIndex = weekIndex,
        school = school
    )

    override val tags: Set<DataTag> = emptySet()
}