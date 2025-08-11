package plus.vplan.app.feature.calendar.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.LessonTime
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.usecase.GetCurrentDateTimeUseCase
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.calendar.domain.usecase.GetFirstLessonStartUseCase
import plus.vplan.app.feature.calendar.domain.usecase.GetHolidaysUseCase
import plus.vplan.app.feature.calendar.domain.usecase.GetLastDisplayTypeUseCase
import plus.vplan.app.feature.calendar.domain.usecase.SetLastDisplayTypeUseCase
import plus.vplan.app.utils.atStartOfMonth
import plus.vplan.app.utils.atStartOfWeek
import plus.vplan.app.utils.inWholeMinutes
import plus.vplan.app.utils.now
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

@OptIn(FlowPreview::class)
class CalendarViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val getCurrentDateTimeUseCase: GetCurrentDateTimeUseCase,
    private val getLastDisplayTypeUseCase: GetLastDisplayTypeUseCase,
    private val setLastDisplayTypeUseCase: SetLastDisplayTypeUseCase,
    private val getFirstLessonStartUseCase: GetFirstLessonStartUseCase,
    private val getHolidaysUseCase: GetHolidaysUseCase
) : ViewModel() {
    var state by mutableStateOf(CalendarState())
        private set

    private val syncJobs = mutableListOf<SyncJob>()
    private val holidays: MutableList<LocalDate> = mutableListOf()

    private fun launchSyncJob(date: LocalDate): Job {
        return syncJobs.firstOrNull { it.date == date }?.job ?: viewModelScope.launch {
            App.daySource.getById(state.currentProfile!!.getSchool().getFirstValue()!!.id.toString() + "/$date", state.currentProfile!!).filterIsInstance<CacheState.Done<Day>>().map { it.data }.collectLatest { day ->
                var selectorDay = DateSelectorDay(
                    date = date,
                    homework = day.homeworkIds.map { DateSelectorDay.HomeworkItem(subject = "", isDone = false) },
                    assessments = day.assessmentIds.map { "" },
                    isHoliday = date in holidays
                )

                var calendarDay = CalendarDay(
                    date = date,
                    info = day.info,
                    dayType = day.dayType,
                    week = day.week?.getFirstValueOld(),
                    assessments = emptyList(),
                    homework = emptyList(),
                    lessons = emptyMap(),
                    layoutedLessons = emptyList()
                )

                fun updateState() {
                    state = state.copy(
                        uiUpdateVersion = state.uiUpdateVersion + 1,
                        calendarDays = state.calendarDays + (date to calendarDay),
                        selecorDays = state.selecorDays + (date to selectorDay)
                    )
                }

                coroutineScope {
                    launch {
                        day.lessons.collectLatest {
                            val lessons = it.groupBy { it.lessonTime.getFirstValueOld()!!.lessonNumber }.mapValues { it.value.sortedBy { it.subject } }
                            val layoutedLessons = it.calculateLayouting()
                            calendarDay = calendarDay.copy(layoutedLessons = layoutedLessons, lessons = lessons.toList().sortedBy { it.first }.toMap())
                            updateState()
                        }
                    }
                    launch {
                        day.assessments.collectLatest {
                            calendarDay = calendarDay.copy(assessments = it.toList())
                            it.map { it.subjectInstance.getFirstValue()?.subject ?: "?" }.sorted().let { selectorDay = selectorDay.copy(assessments = it) }
                            updateState()
                        }
                    }
                    launch {
                        day.homework.collectLatest {
                            calendarDay = calendarDay.copy(homework = it.toList())
                            it
                                .map { DateSelectorDay.HomeworkItem(subject = it.subjectInstance?.getFirstValue()?.subject ?: it.group?.getFirstValue()?.name ?: "?", isDone = state.currentProfile is Profile.StudentProfile && it.tasks.first().all { it.isDone(state.currentProfile as Profile.StudentProfile) }) }
                                .sortedBy { it.subject }
                                .let { selectorDay = selectorDay.copy(homework = it) }
                            updateState()
                        }
                    }
                }
            }
        }.also {
            syncJobs.add(SyncJob(it, date))
        }
    }

    private fun startDaySyncJobsFromSelectedDate() {
        val startOfWeek = state.selectedDate.atStartOfWeek()
        val startOfMonth = startOfWeek.atStartOfMonth()
        val coveredDates = mutableListOf<LocalDate>()
        repeat(2*31) {
            val date = startOfMonth + it.days
            coveredDates.add(date)
            launchSyncJob(date)
        }
    }

    init {
        viewModelScope.launch { getCurrentDateTimeUseCase().debounce(100).collectLatest { state = state.copy(currentTime = it) } }
        viewModelScope.launch { getLastDisplayTypeUseCase().collectLatest { state = state.copy(displayType = it) } }

        viewModelScope.launch {
            getCurrentProfileUseCase().collectLatest { profile ->
                state = state.copy(
                    currentProfile = profile,
                    start = getFirstLessonStartUseCase(profile)
                )
                syncJobs.forEach { it.job.cancel() }
                syncJobs.clear()

                var hasHolidaysInitialized = false

                launch {
                    getHolidaysUseCase(profile)
                        .collectLatest {
                            hasHolidaysInitialized = true
                            holidays.clear()
                            holidays.addAll(it)
                        }
                }

                launch {
                    while (!hasHolidaysInitialized) {
                        delay(10)
                    }
                    startDaySyncJobsFromSelectedDate()
                }

            }
        }
    }

    fun onEvent(event: CalendarEvent) {
        viewModelScope.launch {
            when (event) {
                is CalendarEvent.SelectDate -> {
                    while (state.currentProfile == null) {
                        delay(10)
                        Logger.d { "Waiting for profile" }
                    }
                    state = state.copy(selectedDate = event.date)
                    startDaySyncJobsFromSelectedDate()
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
    val selecorDays: Map<LocalDate, DateSelectorDay> = emptyMap(),
    val calendarDays: Map<LocalDate, CalendarDay> = emptyMap()
)

sealed class CalendarEvent {
    data class SelectDate(val date: LocalDate) : CalendarEvent()

    data class SelectDisplayType(val displayType: DisplayType): CalendarEvent()
}

private data class SyncJob(
    val job: Job,
    val date: LocalDate
)

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
    val dayType: Day.DayType,
    val week: Week?,
    val assessments: List<Assessment>,
    val homework: List<Homework>,
    val lessons: Map<Int, List<Lesson>>?,
    val layoutedLessons: List<LessonLayoutingInfo>
) {
    constructor(date: LocalDate): this(date, null, Day.DayType.UNKNOWN, null, emptyList(), emptyList(), null, emptyList())
}

/**
 * Creates a layout for a calendar view of the given lessons based on their overlap if some exists.
 */
suspend fun Collection<Lesson>.calculateLayouting(): List<LessonLayoutingInfo> {
    val lessons = this.associateWith { it.lessonTime.getFirstValueOld()!! }.toList().sortedBy { it.second.start.inWholeMinutes().toString().padStart(4, '0') + " " + it.first.subject }
    val layoutingInfo = mutableListOf<LessonLayoutingInfo>()
    lessons.forEach { (lesson, lessonTime) ->
        val overlapping = layoutingInfo.filter {
            val lessonStartsDuringOtherLesson = it.lessonTime.start >= lessonTime.start && it.lessonTime.start < lessonTime.end
            val lessonEndsDuringOtherLesson = it.lessonTime.end > lessonTime.start && it.lessonTime.end <= lessonTime.end
            val otherLessonStartsDuringLesson = lessonTime.start >= it.lessonTime.start && lessonTime.start < it.lessonTime.end
            val otherLessonEndsDuringLesson = lessonTime.end > it.lessonTime.start && lessonTime.end <= it.lessonTime.end
            lessonStartsDuringOtherLesson || lessonEndsDuringOtherLesson || otherLessonStartsDuringLesson || otherLessonEndsDuringLesson
        }
        overlapping.onEach { layoutingInfo[layoutingInfo.indexOf(it)] = it.copy(of = it.of + 1) }
        layoutingInfo.add(LessonLayoutingInfo(lesson, lessonTime, overlapping.maxOfOrNull { it.sideShift }?.plus(1) ?: 0, overlapping.size+1))
    }
    return layoutingInfo
}

data class LessonLayoutingInfo(
    val lesson: Lesson,
    val lessonTime: LessonTime,
    val sideShift: Int,
    val of: Int
)

enum class DisplayType {
    Agenda, Calendar
}