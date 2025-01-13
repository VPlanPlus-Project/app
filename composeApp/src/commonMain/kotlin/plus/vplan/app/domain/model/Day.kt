package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.cache.Item
import kotlin.uuid.Uuid

data class Day(
    val id: String,
    val date: LocalDate,
    val school: Int,
    val week: String?,
    val info: String?,
    val dayType: DayType,
    val timetable: List<Uuid>,
    val substitutionPlan: List<Uuid>,
    val nextSchoolDay: String?
): Item {
    enum class DayType {
        REGULAR, WEEKEND, HOLIDAY, UNKNOWN
    }

    companion object {
        fun buildId(school: School, date: LocalDate) = "${school.id}/$date"
    }

    override fun getEntityId(): String = this.id
}