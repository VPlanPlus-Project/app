@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package plus.vplan.app.feature.calendar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import plus.vplan.app.core.model.Day
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Week
import plus.vplan.app.core.utils.date.atStartOfWeek
import plus.vplan.app.domain.model.populated.AssessmentPopulator
import plus.vplan.app.domain.model.populated.HomeworkPopulator
import plus.vplan.app.domain.model.populated.LessonPopulator
import plus.vplan.app.domain.model.populated.PopulatedAssessment
import plus.vplan.app.domain.model.populated.PopulatedDay
import plus.vplan.app.domain.model.populated.PopulatedHomework
import plus.vplan.app.domain.model.populated.PopulatedLesson
import plus.vplan.app.domain.model.populated.PopulationContext
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.usecase.GetCurrentDateTimeUseCase
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.domain.usecase.GetDayUseCase
import plus.vplan.app.feature.calendar.domain.usecase.GetFirstLessonStartUseCase
import plus.vplan.app.feature.calendar.domain.usecase.GetHolidaysUseCase
import plus.vplan.app.feature.calendar.domain.usecase.GetLastDisplayTypeUseCase
import plus.vplan.app.feature.calendar.domain.usecase.SetLastDisplayTypeUseCase
import plus.vplan.app.utils.atStartOfMonth
import plus.vplan.app.utils.inWholeMinutes
import plus.vplan.app.utils.now
import plus.vplan.app.utils.plus
import plus.vplan.app.utils.sortedByKey
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val getCurrentDateTimeUseCase: GetCurrentDateTimeUseCase,
    private val getDayUseCase: GetDayUseCase,
    private val getLastDisplayTypeUseCase: GetLastDisplayTypeUseCase,
    private val setLastDisplayTypeUseCase: SetLastDisplayTypeUseCase,
    private val getFirstLessonStartUseCase: GetFirstLessonStartUseCase,
    private val getHolidaysUseCase: GetHolidaysUseCase,
    private val lessonPopulator: LessonPopulator,
    private val homeworkPopulator: HomeworkPopulator,
    private val assessmentPopulator: AssessmentPopulator,
    private val keyValueRepository: KeyValueRepository,
) : ViewModel() {
    val state: StateFlow<CalendarState>
        field = MutableStateFlow(CalendarState())

    private var dayJobs: Map<LocalDate, Job> = emptyMap()

    init {
        viewModelScope.launch {
            getCurrentDateTimeUseCase()
                .debounce(100)
                .collect { time ->
                    state.update { it.copy(currentTime = time) }
                }
        }

        viewModelScope.launch {
            getLastDisplayTypeUseCase().collect { displayType ->
                state.update { it.copy(displayType = displayType) }
            }
        }

        viewModelScope.launch {
            getCurrentProfileUseCase()
                .filterNotNull()
                .collect { profile ->
                    state.update {
                        it.copy(
                            currentProfile = profile,
                            start = getFirstLessonStartUseCase(profile)
                        )
                    }
                    launchHolidays(profile)
                    launchDaysForMonth(profile)
                }
        }
    }

    private fun launchHolidays(profile: Profile) {
        viewModelScope.launch {
            getHolidaysUseCase(profile).collect { holidays ->
                state.update { it.copy(holidays = holidays.toSet()) }
            }
        }
    }

    private fun launchDaysForMonth(profile: Profile) {
        dayJobs.values.forEach { it.cancel() }
        dayJobs = emptyMap()

        val startOfMonth = state.value.selectedDate.atStartOfWeek().atStartOfMonth()
        repeat(2 * 31) { offset ->
            val date = startOfMonth + offset.days
            dayJobs = dayJobs + (date to launchDay(profile, date))
        }
    }

    private fun launchDay(profile: Profile, date: LocalDate): Job {
        return viewModelScope.launch {
            getDayUseCase(profile, date).collect { populatedDay ->
                updateDayState(date, populatedDay)
            }
        }
    }

    private suspend fun updateDayState(date: LocalDate, day: PopulatedDay) {
        val profile = state.value.currentProfile ?: return

        val lessons = day.substitution.ifEmpty { day.timetable }
            .let { lessonPopulator.populateMultiple(it, PopulationContext.Profile(profile)).first() }

        val populatedHomework = day.homework.toList()
            .let { homeworkPopulator.populateMultiple(it, PopulationContext.Profile(profile)).first() }

        val populatedAssessments = day.assessments.toList()
            .let { assessmentPopulator.populateMultiple(it, PopulationContext.Profile(profile)).first() }

        val lessonsGrouped = lessons
            .groupBy { it.lesson.lessonNumber }
            .mapValues { (_, lessons) -> lessons.sortedBy { it.lesson.subject } }

        val hasTooManyInterpolated = lessons.count { it.lessonTime?.interpolated == false } < lessons.size / 2
        val hasMissingLessonTimes = lessons.any { it.lessonTime == null }

        val forceReduced = keyValueRepository.getBooleanOrDefault(Keys.forceReducedCalendarView.key, false).first()

        val lessonRendering = if (state.value.displayType == DisplayType.Agenda || hasTooManyInterpolated || hasMissingLessonTimes || forceReduced) {
            LessonRendering.ListView(lessonsGrouped)
        } else {
            try {
                LessonRendering.Layouted(lessons.calculateLayouting())
            } catch (_: LessonWithoutTimeException) {
                LessonRendering.ListView(lessonsGrouped)
            }
        }

        val calendarDay = CalendarDay(
            date = date,
            info = day.day.info,
            dayType = day.day.dayType,
            week = day.day.week,
            lessons = lessonRendering,
            assessments = populatedAssessments,
            homework = populatedHomework
        )

        val selectorDay = DateSelectorDay(
            date = date,
            homework = populatedHomework.map { hw ->
                DateSelectorDay.HomeworkItem(
                    subject = hw.subjectInstance?.subject ?: hw.group?.name ?: "?",
                    isDone = profile is Profile.StudentProfile && hw.tasks.all { it.isDone(profile) }
                )
            }.sortedBy { it.subject },
            assessments = populatedAssessments.map { it.subjectInstance.subject },
            isHoliday = state.value.holidays.contains(date)
        )

        state.update {
            it.copy(
                uiUpdateVersion = it.uiUpdateVersion + 1,
                calendarDays = it.calendarDays + (date to calendarDay),
                selectorDays = it.selectorDays + (date to selectorDay)
            )
        }
    }

    fun onEvent(event: CalendarEvent) {
        viewModelScope.launch {
            when (event) {
                is CalendarEvent.SelectDate -> {
                    state.update { it.copy(selectedDate = event.date) }
                    state.value.currentProfile?.let { launchDaysForMonth(it) }
                }
                is CalendarEvent.SelectDisplayType -> {
                    setLastDisplayTypeUseCase(event.displayType)
                }
            }
        }
    }
}

data class CalendarState(
    val selectedDate: LocalDate = LocalDate.now(),
    val currentProfile: Profile? = null,
    val currentTime: LocalDateTime = LocalDateTime.now(),
    val uiUpdateVersion: Int = 0,
    val displayType: DisplayType = DisplayType.Calendar,
    val start: LocalTime = LocalTime(0, 0),
    val holidays: Set<LocalDate> = emptySet(),
    val selectorDays: Map<LocalDate, DateSelectorDay> = emptyMap(),
    val calendarDays: Map<LocalDate, CalendarDay> = emptyMap()
)

sealed class CalendarEvent {
    data class SelectDate(val date: LocalDate) : CalendarEvent()
    data class SelectDisplayType(val displayType: DisplayType): CalendarEvent()
}

data class DateSelectorDay(
    val date: LocalDate,
    val homework: List<HomeworkItem>,
    val assessments: List<String>,
    val isHoliday: Boolean
) {
    constructor(date: LocalDate) : this(date, emptyList(), emptyList(), false)

    data class HomeworkItem(
        val subject: String,
        val isDone: Boolean
    )
}

data class CalendarDay(
    val date: LocalDate,
    val info: String? = null,
    val dayType: Day.DayType = Day.DayType.REGULAR,
    val week: Week? = null,
    val assessments: List<PopulatedAssessment> = emptyList(),
    val homework: List<PopulatedHomework> = emptyList(),
    val lessons: LessonRendering? = null
)

suspend fun Collection<PopulatedLesson>.calculateLayouting(): List<LessonLayoutingInfo> {
    this.firstOrNull { it.lessonTime == null }?.let { throw LessonWithoutTimeException(it) }

    return withContext(Dispatchers.Default) {
        val lessons = this@calculateLayouting
            .toList()
            .filter { it.lessonTime != null }
            .sortedBy { it.lessonTime!!.start.inWholeMinutes().toString().padStart(4, '0') + " " + it.lesson.subject }

        val layoutingInfo = mutableListOf<LessonLayoutingInfo>()

        data class Event(val time: Long, val isStart: Boolean, val lesson: PopulatedLesson)

        val events = lessons.flatMap { populatedLesson ->
            listOf(
                Event(populatedLesson.lessonTime!!.start.inWholeMinutes().toLong(), true, populatedLesson),
                Event(populatedLesson.lessonTime!!.end.inWholeMinutes().toLong(), false, populatedLesson)
            )
        }

        val eventsByTime = events.groupBy { it.time }.sortedByKey()

        val active = mutableListOf<LessonLayoutingInfo>()

        for ((_, evs) in eventsByTime) {
            evs.filter { !it.isStart }.forEach { endEvent ->
                active.removeAll { it.lesson == endEvent.lesson }
            }

            val startEvents = evs.filter { it.isStart }
            if (startEvents.isNotEmpty()) {
                val occupied = active.map { it.sideShift }.toMutableSet()

                val newInfos = startEvents.map { se ->
                    var shift = 0
                    while (shift in occupied) shift++
                    occupied.add(shift)
                    LessonLayoutingInfo(se.lesson, shift, 0)
                }

                layoutingInfo.addAll(newInfos)
                active.addAll(newInfos)

                val activeSnapshot = active.toList()
                for (a in activeSnapshot) {
                    val overlaps = activeSnapshot.count { other ->
                        other.lesson.lessonTime!!.start < a.lesson.lessonTime!!.end && other.lesson.lessonTime!!.end > a.lesson.lessonTime!!.start
                    }
                    val i = layoutingInfo.indexOfFirst { it.lesson == a.lesson }
                    if (i >= 0) layoutingInfo[i] = layoutingInfo[i].copy(of = overlaps)
                }
            }
        }

        return@withContext layoutingInfo
    }
}

data class LessonLayoutingInfo(
    val lesson: PopulatedLesson,
    val sideShift: Int,
    val of: Int
)

enum class DisplayType {
    Agenda, Calendar
}

sealed class LessonRendering {
    data class ListView(val lessons: Map<Int, List<PopulatedLesson>>) : LessonRendering()
    data class Layouted(val lessons: List<LessonLayoutingInfo>) : LessonRendering()

    val size: Int
        get() = when (this) {
            is ListView -> lessons.size
            is Layouted -> lessons.size
        }
}

sealed class LessonLayoutingException(message: String) : Exception(message)
class LessonWithoutTimeException(lesson: PopulatedLesson) : LessonLayoutingException("Lesson ${lesson.lesson.id} has no lesson time")
