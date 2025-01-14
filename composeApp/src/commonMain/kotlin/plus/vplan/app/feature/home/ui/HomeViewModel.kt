package plus.vplan.app.feature.home.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.catch
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
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.usecase.GetCurrentDateTimeUseCase
import plus.vplan.app.domain.usecase.GetDayUseCase
import plus.vplan.app.feature.home.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateHolidaysUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateTimetableUseCase

private val LOGGER = Logger.withTag("HomeViewModel")

class HomeViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val getCurrentDateTimeUseCase: GetCurrentDateTimeUseCase,
    private val getDayUseCase: GetDayUseCase,
    private val updateSubstitutionPlanUseCase: UpdateSubstitutionPlanUseCase,
    private val updateTimetableUseCase: UpdateTimetableUseCase,
    private val updateHolidaysUseCase: UpdateHolidaysUseCase,
) : ViewModel() {
    var state by mutableStateOf(HomeState())
        private set

    init {
        viewModelScope.launch {
            getCurrentProfileUseCase().collectLatest { profile ->
                state = state.copy(currentProfile = profile)
                getDayUseCase(profile, state.currentTime.date)
                    .catch { e -> LOGGER.e { "Something went wrong on retrieving the day for Profile ${profile.id} (${profile.name}) at ${state.currentTime.date}:\n${e.stackTraceToString()}" } }
                    .collectLatest { day ->
                        state = state.copy(currentDay = HomeViewDay(
                            day = day,
                            timetable = day.timetable.map { App.timetableSource.getById(it).filterIsInstance<CacheState.Done<Lesson.TimetableLesson>>().map { lesson -> lesson.data }.first() }.filter { it.isRelevantForProfile(profile) }.onEach { it.prefetch() }
                        ))
                        if (day.nextSchoolDay != null)getDayUseCase(profile, LocalDate.parse(day.nextSchoolDay.split("/")[1])).collectLatest { nextDay ->
                            state = state.copy(nextDay = HomeViewDay(
                                day = nextDay,
                                timetable = nextDay.timetable.map { App.timetableSource.getById(it).filterIsInstance<CacheState.Done<Lesson.TimetableLesson>>().map { lesson -> lesson.data }.first() }.filter { it.isRelevantForProfile(profile) }.onEach { it.prefetch() }
                            ))
                        }
                    }
            }
        }
        viewModelScope.launch {
            getCurrentDateTimeUseCase().collect { time ->
                state = state.copy(currentTime = time)
            }
        }
    }

    private fun update() {
        state = state.copy(isUpdating = true)
        viewModelScope.launch {
            updateHolidaysUseCase(state.currentProfile!!.getSchool().getFirstValue() as School.IndiwareSchool)
            updateTimetableUseCase(state.currentProfile!!.getSchool().getFirstValue() as School.IndiwareSchool)
            updateSubstitutionPlanUseCase(state.currentProfile!!.getSchool().getFirstValue() as School.IndiwareSchool, state.currentTime.date)
        }.invokeOnCompletion { state = state.copy(isUpdating = false) }
    }

    fun onEvent(event: HomeEvent) {
        viewModelScope.launch {
            when (event) {
                HomeEvent.OnRefresh -> update()
            }
        }
    }
}

data class HomeState(
    val currentProfile: Profile? = null,
    val currentTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val currentDay: HomeViewDay? = null,
    val nextDay: HomeViewDay? = null,
    val isUpdating: Boolean = false
)

sealed class HomeEvent {
    data object OnRefresh : HomeEvent()
}

data class HomeViewDay(
    val day: Day,
    val timetable: List<Lesson.TimetableLesson>
)

private suspend fun Lesson.isRelevantForProfile(profile: Profile): Boolean {
    when (profile) {
        is Profile.StudentProfile -> {
            if (profile.group !in this.groups) return false
            if (profile.defaultLessons.filterValues { false }.any { it.key == this.defaultLesson }) return false
            if (this is Lesson.TimetableLesson) {
                val defaultLessons = profile.defaultLessons.mapKeys { profile.getDefaultLesson(it.key) }
                if (defaultLessons.filterValues { !it }.any { it.key.getCourseItem()?.name == this.subject }) return false
                if (defaultLessons.filterValues { !it }.any { it.key.course == null && it.key.subject == this.subject }) return false
                defaultLessons.isEmpty()
            }
        }
        is Profile.TeacherProfile -> {
            if (profile.teacher !in this.teachers) return false
        }
        is Profile.RoomProfile -> {
            if (profile.room !in this.rooms.orEmpty()) return false
        }
    }
    return true
}

private suspend fun Lesson.prefetch() {
    this.getLessonTimeItem()
}