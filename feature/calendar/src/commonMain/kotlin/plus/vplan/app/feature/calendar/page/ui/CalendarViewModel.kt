@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package plus.vplan.app.feature.calendar.page.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import plus.vplan.app.core.common.domain.usecase.GetCurrentDateTimeUseCase
import plus.vplan.app.core.common.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.core.common.domain.usecase.GetDayUseCase
import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.Day
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Week
import plus.vplan.app.core.utils.date.atStartOfMonth
import plus.vplan.app.core.utils.date.atStartOfWeek
import plus.vplan.app.core.utils.date.inWholeMinutes
import plus.vplan.app.core.utils.date.now
import plus.vplan.app.core.utils.date.plus
import plus.vplan.app.feature.calendar.page.domain.model.DateSelectorDay
import plus.vplan.app.feature.calendar.page.domain.model.DisplayType
import plus.vplan.app.feature.calendar.page.domain.usecase.DownloadDayIfNecessaryUseCase
import plus.vplan.app.feature.calendar.page.domain.usecase.GetFirstLessonStartUseCase
import plus.vplan.app.feature.calendar.page.domain.usecase.GetHolidaysUseCase
import plus.vplan.app.feature.calendar.page.domain.usecase.GetLastDisplayTypeUseCase
import plus.vplan.app.feature.calendar.page.domain.usecase.SetLastDisplayTypeUseCase
import plus.vplan.app.feature.calendar.view.domain.model.LessonLayoutingInfo
import plus.vplan.app.feature.calendar.view.domain.model.LessonRendering
import kotlin.time.Duration.Companion.days

class CalendarViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val getCurrentDateTimeUseCase: GetCurrentDateTimeUseCase,
    private val getDayUseCase: GetDayUseCase,
    private val getLastDisplayTypeUseCase: GetLastDisplayTypeUseCase,
    private val setLastDisplayTypeUseCase: SetLastDisplayTypeUseCase,
    private val getFirstLessonStartUseCase: GetFirstLessonStartUseCase,
    private val getHolidaysUseCase: GetHolidaysUseCase,
    private val keyValueRepository: KeyValueRepository,
    private val downloadDayIfNecessaryUseCase: DownloadDayIfNecessaryUseCase,
) : ViewModel() {
    val state: StateFlow<CalendarState>
        field = MutableStateFlow(CalendarState())

    private var dayJobs: Map<LocalDate, Job> = emptyMap()

    init {
        viewModelScope.launch(CoroutineName(this::class.qualifiedName + ".Init.DateTime")) {
            getCurrentDateTimeUseCase()
                .debounce(100)
                .collect { time ->
                    state.update { it.copy(currentTime = time) }
                }
        }

        viewModelScope.launch(CoroutineName(this::class.qualifiedName + ".Init.DisplayType")) {
            downloadDayIfNecessaryUseCase.isRunning.collect { isRunning ->
                state.update { it.copy(isTimetableUpdating = isRunning) }
            }
        }

        viewModelScope.launch {
            getLastDisplayTypeUseCase().collect { displayType ->
                state.update { it.copy(displayType = displayType) }
            }
        }

        viewModelScope.launch(CoroutineName(this::class.qualifiedName + ".Init.Profile")) {
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
        viewModelScope.launch(CoroutineName(this::class.qualifiedName + ".LaunchHolidays")) {
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
        return viewModelScope.launch(CoroutineName(this::class.qualifiedName + ".LaunchDay")) {
            launch {
                downloadDayIfNecessaryUseCase(date, profile.school)
            }
            keyValueRepository.get(Keys.forceReducedCalendarView.key)
                .map { it.toBoolean() }
                .collectLatest { forceReduced ->
                    getDayUseCase(profile, date)
                        .distinctUntilChangedBy { day ->
                            val timetableIds = day.timetable.map { it.id }.sorted()
                            val substitutionIds = day.substitution.map { it.id }.sorted()
                            val homeworkIds = day.homework.map { it.id }.sorted()
                            val assessmentIds = day.assessments.map { it.id }.sorted()
                            "${day.day.id}|${day.day.dayType}|${day.day.info}|$timetableIds|$substitutionIds|$homeworkIds|$assessmentIds"
                        }
                        .flowOn(Dispatchers.Default)
                        .collect { day ->
                            val lessons = day.substitution.ifEmpty { day.timetable }

                            val lessonsGrouped = lessons
                                .groupBy { it.lessonNumber }
                                .mapValues { (_, l) -> l.sortedBy { it.subject } }

                            val hasTooManyInterpolated =
                                lessons.count { it.lessonTime?.interpolated == false } < lessons.size / 2
                            val hasMissingLessonTimes = lessons.any { it.lessonTime == null }

                            val lessonRendering =
                                if (state.value.displayType == DisplayType.Agenda || hasTooManyInterpolated || hasMissingLessonTimes || forceReduced) {
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
                                assessments = day.assessments,
                                homework = day.homework,
                            )

                            val selectorDay = DateSelectorDay(
                                date = date,
                                homework = day.homework.map { hw ->
                                    val subject = hw.subjectInstance?.subject
                                        ?: hw.group?.name
                                        ?: "?"

                                    val allTasksDone = profile is Profile.StudentProfile && hw.tasks.all {
                                        it.isDone(profile)
                                    }

                                    DateSelectorDay.HomeworkItem(
                                        subject = subject,
                                        isDone = allTasksDone
                                    )
                                }.sortedBy { it.subject },
                                assessments = day.assessments.map { it.subjectInstance.subject },
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
                }
        }
    }

    fun onEvent(event: CalendarEvent) {
        when (event) {
            is CalendarEvent.SelectDate -> {
                state.update { it.copy(selectedDate = event.date) }
                state.value.currentProfile?.let { launchDaysForMonth(it) }
            }

            is CalendarEvent.SelectDisplayType -> {
                viewModelScope.launch(CoroutineName(this::class.qualifiedName + ".Action.SelectDisplayType")) {
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
    val calendarDays: Map<LocalDate, CalendarDay> = emptyMap(),
    val isTimetableUpdating: Boolean = false,
)

sealed class CalendarEvent {
    data class SelectDate(val date: LocalDate) : CalendarEvent()
    data class SelectDisplayType(val displayType: DisplayType) : CalendarEvent()
}


data class CalendarDay(
    val date: LocalDate,
    val info: String? = null,
    val dayType: Day.DayType = Day.DayType.REGULAR,
    val week: Week? = null,
    val assessments: List<Assessment> = emptyList(),
    val homework: List<Homework> = emptyList(),
    val lessons: LessonRendering? = null,
)

suspend fun Collection<Lesson>.calculateLayouting(): List<LessonLayoutingInfo> {

    this.firstOrNull { it.lessonTime == null }?.let { throw LessonWithoutTimeException(it) }

    return withContext(Dispatchers.Default) {

        val lessons = this@calculateLayouting
            .filter { lesson -> lesson.lessonTime != null }
            .sortedWith(
                compareBy<Lesson> { lesson -> lesson.lessonTime!!.start }
                    .thenBy(nullsLast()) { lesson -> lesson.subject }
                    .thenBy(nullsLast()) { lesson -> lesson.teachers.map { it.name }.sorted().joinToString() }
            )
            .sortedBy { it.lessonTime!!.start.inWholeMinutes() }

        data class Event(val time: Int, val isStart: Boolean, val lesson: Lesson)

        val events = lessons.flatMap { lesson ->
            listOf(
                Event(lesson.lessonTime!!.start.inWholeMinutes(), true, lesson),
                Event(lesson.lessonTime!!.end.inWholeMinutes(), false, lesson)
            )
        }.sortedWith(
            compareBy<Event> { it.time }
                .thenBy { if (it.isStart) 1 else 0 } // end events first at same time
        )

        val active = mutableListOf<LessonLayoutingInfo>()
        val result = mutableListOf<LessonLayoutingInfo>()

        for (event in events) {

            if (!event.isStart) {
                active.removeAll { it.lesson == event.lesson }
                continue
            }

            // Determine lowest free column
            val usedColumns = active.map { it.sideShift }.toMutableSet()
            var column = 0
            while (column in usedColumns) column++

            val info = LessonLayoutingInfo(
                lesson = event.lesson,
                sideShift = column,
                of = 0 // temporary
            )

            active.add(info)
            result.add(info)

            // Update overlap count for current overlap group
            val overlapCount = active.size

            active.forEach { a ->
                val index = result.indexOfFirst { it.lesson == a.lesson }
                if (index >= 0) {
                    result[index] = result[index].copy(of = overlapCount)
                }
            }
        }

        result
    }
}

sealed class LessonLayoutingException(message: String) : Exception(message)
class LessonWithoutTimeException(lesson: Lesson) :
    LessonLayoutingException("Lesson ${lesson.id} has no lesson time")
