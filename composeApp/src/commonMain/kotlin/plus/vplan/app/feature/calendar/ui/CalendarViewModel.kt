package plus.vplan.app.feature.calendar.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
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
import plus.vplan.app.utils.atStartOfMonth
import plus.vplan.app.utils.atStartOfWeek
import plus.vplan.app.utils.now
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

class CalendarViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val getCurrentDateTimeUseCase: GetCurrentDateTimeUseCase,
) : ViewModel() {
    var state by mutableStateOf(CalendarState())
        private set

    private val syncJobs = mutableListOf<SyncJob>()

    private fun launchSyncJob(date: LocalDate, syncLessons: Boolean): Job {
        return viewModelScope.launch {
            App.daySource.getById(state.currentProfile!!.getSchool().getFirstValue()!!.id.toString() + "/$date").filterIsInstance<CacheState.Done<Day>>().map { it.data }.collectLatest { day ->
                if (!syncLessons) state = state.copy(days = state.days + (date to CalendarDay(day)))
                else {
                    val timetable = day.timetable.map { App.timetableSource.getById(it).filterIsInstance<CacheState.Done<Lesson.TimetableLesson>>().map { it.data }.first() }.filter { it.isRelevantForProfile(state.currentProfile!!) }.onEach { it.prefetch() }
                    val substitutionPlan = day.substitutionPlan.map { App.substitutionPlanSource.getById(it).filterIsInstance<CacheState.Done<Lesson.SubstitutionPlanLesson>>().map { it.data }.first() }.filter { it.isRelevantForProfile(state.currentProfile!!) }.onEach { it.prefetch() }.ifEmpty { null }
                    state = state.copy(days = state.days + (date to CalendarDay(day, timetable, substitutionPlan)))
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            getCurrentDateTimeUseCase().collectLatest { state = state.copy(currentTime = it) }
        }
        viewModelScope.launch {
            getCurrentProfileUseCase().collectLatest { profile ->
                state = state.copy(currentProfile = profile)
                syncJobs.forEach { it.job.cancel() }
                syncJobs.clear()
                repeat(7) {
                    val date = LocalDate.now().atStartOfWeek() + it.days
                    syncJobs.add(
                        SyncJob(
                            job = launchSyncJob(date, true),
                            date = date,
                            syncLessons = true
                        )
                    )
                }

                repeat(31) {
                    val date = LocalDate.now().atStartOfMonth() + it.days
                    if (syncJobs.any { it.date == date }) return@repeat
                    syncJobs.add(
                        SyncJob(
                            job = launchSyncJob(date, false),
                            date = date,
                            syncLessons = false
                        )
                    )
                }
            }

        }
    }

    fun onEvent(event: CalendarEvent) {
        viewModelScope.launch {
            when (event) {
                is CalendarEvent.SelectDate -> {
                    state = state.copy(selectedDate = event.date)
                    if (syncJobs.any { it.date == event.date && !it.syncLessons }) {
                        syncJobs.find { it.date == event.date && it.syncLessons }?.job?.cancel()
                        syncJobs.removeAll { it.date == event.date && it.syncLessons }
                    }
                    syncJobs.add(
                        SyncJob(
                            job = launchSyncJob(event.date, true),
                            date = event.date,
                            syncLessons = true
                        )
                    )
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
)

sealed class CalendarEvent {
    data class SelectDate(val date: LocalDate) : CalendarEvent()
}

private data class SyncJob(
    val job: Job,
    val date: LocalDate,
    val syncLessons: Boolean,
)

data class CalendarDay(
    val day: Day,
    val timetable: List<Lesson.TimetableLesson>,
    val substitutionPlan: List<Lesson.SubstitutionPlanLesson>?
) {
    constructor(day: Day) : this(day, emptyList(), emptyList())

    val lessons: List<Lesson>
        get() = substitutionPlan?.ifEmpty { timetable } ?: timetable
}

private suspend fun Lesson.prefetch() {
    this.getLessonTimeItem()
    this.getRoomItems()
    this.getTeacherItems()
}