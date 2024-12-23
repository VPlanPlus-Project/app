package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDate

data class Day(
    val id: String,
    val date: LocalDate,
    val school: School,
    val week: Week,
    val info: String?
) {
    constructor(
        date: LocalDate,
        school: School,
        week: Week,
        info: String?
    ) : this("${school.id}/$date", date, school, week, info)
}