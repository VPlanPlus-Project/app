package plus.vplan.app.core.model

import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

data class Day(
    val id: String,
    val date: LocalDate,
    val school: School.AppSchool,
    val week: Week?,
    val info: String?,
    val dayType: DayType,
    val nextSchoolDay: LocalDate?,
) {
    enum class DayType {
        REGULAR, WEEKEND, HOLIDAY
    }

    companion object {
        fun buildId(school: School, date: LocalDate) = "${school.aliases}/$date"
        fun buildId(schoolId: Uuid, date: LocalDate) = "$schoolId/$date"
    }
}