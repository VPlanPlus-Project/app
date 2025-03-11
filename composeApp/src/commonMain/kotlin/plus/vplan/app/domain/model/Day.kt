package plus.vplan.app.domain.model

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.Item
import plus.vplan.app.domain.cache.getFirstValue
import kotlin.uuid.Uuid

data class Day(
    val id: String,
    val date: LocalDate,
    val school: Int,
    val weekId: String?,
    val info: String?,
    val dayType: DayType,
    val timetable: Set<Uuid>,
    val substitutionPlan: Set<Uuid>,
    val assessmentIds: Set<Int>,
    val homeworkIds: Set<Int>,
    val nextSchoolDay: String?
): Item {
    enum class DayType {
        REGULAR, WEEKEND, HOLIDAY, UNKNOWN
    }

    companion object {
        fun buildId(school: School, date: LocalDate) = "${school.id}/$date"
    }

    override fun getEntityId(): String = this.id

    val assessments by lazy {
        if (this.assessmentIds.isEmpty()) return@lazy flowOf(emptySet())
        combine(this.assessmentIds.map { assessmentId ->
            App.assessmentSource.getById(assessmentId)
                .filterIsInstance<CacheState.Done<Assessment>>().map { it.data }
        }) { it.toSet() }
    }

    @OptIn(FlowPreview::class)
    val homework by lazy {
        if (this.homeworkIds.isEmpty()) return@lazy flowOf(emptySet())
        combine(this.homeworkIds.map { homeworkId -> App.homeworkSource.getById(homeworkId) }) {
            it.filterIsInstance<CacheState.Done<Homework>>().map { it.data }.toSet()
        }.debounce(50)
    }

    val week by lazy { if (this.weekId == null) return@lazy null else App.weekSource.getById(weekId) }

    val lessons: Flow<Set<Lesson>> by lazy {
        if (timetable.isEmpty()) return@lazy flowOf(emptySet())
        (if (substitutionPlan.isEmpty()) combine(timetable.map { App.timetableSource.getById(it).filterIsInstance<CacheState.Done<Lesson.TimetableLesson>>().map { it.data } }) { it.toSet() }
        else combine(substitutionPlan.map { App.substitutionPlanSource.getById(it).filterIsInstance<CacheState.Done<Lesson.SubstitutionPlanLesson>>().map { it.data } }) { it.toSet() })
            .map { lessons ->
                val week = this.week?.getFirstValue()
                lessons.filter { lesson ->
                    lesson is Lesson.SubstitutionPlanLesson || (lesson is Lesson.TimetableLesson && (lesson.weekType == null || week?.weekType == lesson.weekType))
            }.toSet() }
    }
}