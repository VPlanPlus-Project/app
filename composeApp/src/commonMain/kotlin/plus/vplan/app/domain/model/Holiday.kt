package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDate

data class Holiday(
    val id: String,
    val date: LocalDate,
    val school: School
) {
    constructor(date: LocalDate, school: School) : this(id = "${school.id}/$date", date = date, school = school)
}