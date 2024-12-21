package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDate

data class Week(
    val id: String,
    val calendarWeek: Int,
    val start: LocalDate,
    val end: LocalDate,
    val weekType: String,
    val weekIndex: Int,
    val school: School
)
