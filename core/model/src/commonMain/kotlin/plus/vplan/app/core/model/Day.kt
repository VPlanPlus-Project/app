package plus.vplan.app.core.model

import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

data class Day(
    override val id: String,
    val date: LocalDate,
    val schoolId: Uuid,
    val weekId: String?,
    val info: String?,
    val dayType: DayType,
    val timetable: Set<Uuid>,
    val substitutionPlan: Set<Uuid>,
    val assessmentIds: Set<Int>,
    val homeworkIds: Set<Int>,
    val nextSchoolDayId: String?,
    override val tags: Set<DayTags>
): Item<String, Day.DayTags> {
    enum class DayType {
        REGULAR, WEEKEND, HOLIDAY, UNKNOWN
    }

    companion object {
        fun buildId(school: School, date: LocalDate) = "${school.id}/$date"
        fun buildId(schoolId: Uuid, date: LocalDate) = "$schoolId/$date"
    }

    enum class DayTags: DataTag {
        HAS_METADATA {
            override fun toString(): String {
                return "Has Metadata"
            }
        }, HAS_LESSONS {
            override fun toString(): String {
                return "Has Lessons"
            }
        }
    }
}