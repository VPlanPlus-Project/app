package plus.vplan.app.feature.calendar.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetCurrentDateTimeUseCase
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.calendar.domain.usecase.GetFirstLessonStartUseCase
import plus.vplan.app.feature.calendar.domain.usecase.GetLastDisplayTypeUseCase
import plus.vplan.app.feature.calendar.domain.usecase.SetLastDisplayTypeUseCase
import plus.vplan.app.utils.atStartOfMonth
import plus.vplan.app.utils.atStartOfWeek
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

private const val PREVIEW_DAYS_WITH_LESSONS_FROM_MONDAY = 14

class CalendarViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val getCurrentDateTimeUseCase: GetCurrentDateTimeUseCase,
    private val getLastDisplayTypeUseCase: GetLastDisplayTypeUseCase,
    private val setLastDisplayTypeUseCase: SetLastDisplayTypeUseCase,
    private val getFirstLessonStartUseCase: GetFirstLessonStartUseCase
) : ViewModel() {
    var state by mutableStateOf(CalendarState())
        private set

    private val syncJobs = mutableListOf<SyncJob>()

    private fun launchSyncJob(date: LocalDate, syncLessons: Boolean): Job {
        syncJobs
            .filter { it.date == date && it.syncLessons == syncLessons }
            .onEach { it.job.cancel(); syncJobs.remove(it) }
        return viewModelScope.launch {
            App.daySource.getById(state.currentProfile!!.getSchool().getFirstValue()!!.id.toString() + "/$date", state.currentProfile!!).filterIsInstance<CacheState.Done<Day>>().map { it.data }.collectLatest { day ->
                val lessons = if (syncLessons) day.lessons.first().onEach { it.prefetch() } else null
                state = state.copy(days = state.days + (date to CalendarDay(
                    day = day,
                    lessons = lessons,
                    homework = day.homeworkIds.size,
                    assessments = day.assessmentIds.size
                )))
            }
        }.also {
            syncJobs.add(SyncJob(it, date, syncLessons))
        }
    }

    private fun startDaySyncJobsFromSelectedDate() {
        val startOfWeek = state.selectedDate.atStartOfWeek()
        val startOfMonth = startOfWeek.atStartOfMonth()
        repeat(PREVIEW_DAYS_WITH_LESSONS_FROM_MONDAY) {
            val date = startOfWeek + it.days
            launchSyncJob(date, true)
        }
        repeat(2*31) {
            val date = startOfMonth + it.days
            syncJobs.firstOrNull { it.date == date && it.syncLessons && it.date !in startOfWeek..startOfWeek.plus(PREVIEW_DAYS_WITH_LESSONS_FROM_MONDAY.days) }?.also { syncJobs.remove(it) }?.job?.cancel()
            if (syncJobs.none { it.date == date }) syncJobs.add(SyncJob(launchSyncJob(date, false), date, syncLessons = false))
        }
    }

    init {
        viewModelScope.launch { getCurrentDateTimeUseCase().collectLatest { state = state.copy(currentTime = it) } }
        viewModelScope.launch { getLastDisplayTypeUseCase().collectLatest { state = state.copy(displayType = it) } }

        viewModelScope.launch {
            getCurrentProfileUseCase().collectLatest { profile ->
                state = state.copy(
                    currentProfile = profile,
                    start = getFirstLessonStartUseCase(profile)
                )
                syncJobs.forEach { it.job.cancel() }
                syncJobs.clear()
                startDaySyncJobsFromSelectedDate()
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
    val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val currentProfile: Profile? = null,
    val currentTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val days: Map<LocalDate, CalendarDay> = emptyMap(),
    val displayType: DisplayType = DisplayType.Calendar,
    val start: LocalTime = LocalTime(0, 0)
)

sealed class CalendarEvent {
    data class SelectDate(val date: LocalDate) : CalendarEvent()

    data class SelectDisplayType(val displayType: DisplayType): CalendarEvent()
}

private data class SyncJob(
    val job: Job,
    val date: LocalDate,
    val syncLessons: Boolean,
)

data class CalendarDay(
    val day: Day,
    val lessons: Set<Lesson>?,
    val homework: Int,
    val assessments: Int
) {
    constructor(day: Day) : this(day, emptySet(), 0, 0)
    constructor(date: LocalDate) : this(Day(id = "", date, -1, null, null, Day.DayType.UNKNOWN, emptySet(), emptySet(), emptySet(), emptySet(), null))
}

private suspend fun Lesson.prefetch() {
    this.getLessonTimeItem()
    this.getRoomItems()
    this.getTeacherItems()
    if (this is Lesson.SubstitutionPlanLesson) this.getSubjectInstance()
}

enum class DisplayType {
    Agenda, Calendar
}