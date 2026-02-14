@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.domain.model

import androidx.compose.runtime.Stable
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.DataTag
import plus.vplan.app.core.model.getFirstValueOld
import plus.vplan.app.core.model.Item
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Stable
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

    val assessments by lazy {
        if (this.assessmentIds.isEmpty()) flowOf(emptySet())
        else combine(this.assessmentIds.map { assessmentId ->
            App.assessmentSource.getById(assessmentId)
                .filterIsInstance<CacheState.Done<Assessment>>().map { it.data }
        }) { it.toSet() }
    }

    @OptIn(FlowPreview::class)
    val homework by lazy {
        if (this.homeworkIds.isEmpty()) flowOf(emptySet())
        else combine(this.homeworkIds.map { homeworkId -> App.homeworkSource.getById(homeworkId) }) {
            it.filterIsInstance<CacheState.Done<Homework>>().map { it.data }.toSet()
        }.debounce(50)
    }

    val school by lazy { App.schoolSource.getById(TODO()) }
    val week by lazy { if (this.weekId == null) null else App.weekSource.getById(weekId) }
    val nextSchoolDay by lazy { if (this.nextSchoolDayId == null) null else App.daySource.getById(nextSchoolDayId) }

    val lessons: Flow<Set<Lesson>> by lazy {
        if (timetable.isEmpty() && substitutionPlan.isEmpty()) flowOf(emptySet())
        else {
            (if (substitutionPlan.isEmpty()) combine(timetable.map { App.timetableSource.getById(it).filterIsInstance<CacheState.Done<Lesson.TimetableLesson>>().map { it.data } }) { it.toSet() }
            else combine(substitutionPlan.map { App.substitutionPlanSource.getById(it).filterIsInstance<CacheState.Done<Lesson.SubstitutionPlanLesson>>().map { it.data } }) { it.toSet() })
                .map { lessons ->
                    val week = this.week?.getFirstValueOld()
                    lessons.filter { lesson ->
                        lesson is Lesson.SubstitutionPlanLesson || (lesson is Lesson.TimetableLesson && (lesson.weekType == null || week?.weekType == lesson.weekType))
                    }.toSet() }
        }
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