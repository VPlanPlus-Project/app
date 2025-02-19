package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Item
import plus.vplan.app.domain.cache.getFirstValue
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

    var substitutionPlanItems: List<Lesson.SubstitutionPlanLesson>? = null
        private set

    suspend fun getSubstitutionPlanItems(): List<Lesson.SubstitutionPlanLesson> {
        if (substitutionPlanItems == null) substitutionPlanItems = substitutionPlan.mapNotNull { App.substitutionPlanSource.getById(it).getFirstValue() }.also { this.substitutionPlanItems = it }
        return substitutionPlanItems!!
    }

    var timetableItems: List<Lesson.TimetableLesson>? = null
        private set

    suspend fun getTimetableItems(): List<Lesson.TimetableLesson> {
        if (timetableItems == null) timetableItems = timetable.mapNotNull { App.timetableSource.getById(it).getFirstValue() }.also { this.timetableItems = it }
        return timetableItems!!
    }

    suspend fun getLessonItems(): List<Lesson> {
        if (this.dayType == DayType.REGULAR) return getSubstitutionPlanItems().ifEmpty { getTimetableItems() }
        return emptyList()
    }

    companion object {
        fun buildId(school: School, date: LocalDate) = "${school.id}/$date"
    }

    override fun getEntityId(): String = this.id
}