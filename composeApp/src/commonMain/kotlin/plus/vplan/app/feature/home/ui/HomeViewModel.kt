package plus.vplan.app.feature.home.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.News
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.usecase.GetCurrentDateTimeUseCase
import plus.vplan.app.domain.usecase.GetDayUseCase
import plus.vplan.app.feature.home.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.home.domain.usecase.GetNewsUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateHolidaysUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateTimetableUseCase
import plus.vplan.app.utils.minus
import plus.vplan.app.utils.now
import plus.vplan.app.utils.sortedBySuspending
import plus.vplan.app.utils.until
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi

private val LOGGER = Logger.withTag("HomeViewModel")

@OptIn(ExperimentalUuidApi::class, FlowPreview::class)
class HomeViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val getCurrentDateTimeUseCase: GetCurrentDateTimeUseCase,
    private val getDayUseCase: GetDayUseCase,
    private val updateSubstitutionPlanUseCase: UpdateSubstitutionPlanUseCase,
    private val updateTimetableUseCase: UpdateTimetableUseCase,
    private val updateHolidaysUseCase: UpdateHolidaysUseCase,
    private val getNewsUseCase: GetNewsUseCase
) : ViewModel() {
    var state by mutableStateOf(HomeState())
        private set

    init {
        viewModelScope.launch {
            var newsJob: Job? = null
            var specialLessonsUpdateJob: Job? = null
            getCurrentProfileUseCase().collectLatest { profile ->
                state = state.copy(
                    currentProfile = profile,
                    day = null,
                    initDone = false
                )
                newsJob?.cancel()
                newsJob = launch { getNewsUseCase(profile).collectLatest { state = state.copy(news = it) } }

                specialLessonsUpdateJob?.cancel()
                specialLessonsUpdateJob = launch {
                    var lastSpecialLessonUpdate = LocalDateTime.now() - 1.hours
                    getCurrentDateTimeUseCase()
                        .onEach { time -> state = state.copy(currentTime = time) }
                        .collect { time ->
                            if (lastSpecialLessonUpdate until time < 5.seconds) return@collect
                            if (state.day?.date == time.date) {
                                val currentLessons = state.day?.lessons?.first().orEmpty()
                                    .filter { lesson ->
                                        val lessonTimeItem = lesson.lessonTime.getFirstValueOld()!!
                                        time.time in lessonTimeItem.start..lessonTimeItem.end
                                    }.map { lesson ->
                                        val lessonTimeItem = lesson.lessonTime.getFirstValueOld()!!
                                        CurrentLesson(
                                            lesson = lesson,
                                            continuing = state.day?.lessons?.first().orEmpty().firstOrNull {
                                                val nextLessonTimeItem = it.lessonTime.getFirstValueOld()!!
                                                it.subject != null && it.subject == lesson.subject && it.subjectInstanceId == lesson.subjectInstanceId && nextLessonTimeItem.lessonNumber == lessonTimeItem.lessonNumber + 1
                                            }
                                        )
                                    }

                                val nextLessons = state.day?.lessons?.first().orEmpty()
                                    .filter { lesson ->
                                        val lessonTimeItem = lesson.lessonTime.getFirstValueOld()!!
                                        lessonTimeItem.start > time.time
                                    }
                                    .groupBy { it.lessonTimeId }
                                    .toList()
                                    .associate {
                                        App.lessonTimeSource.getById(it.first).getFirstValueOld()!! to it.second
                                    }
                                    .minByOrNull { it.key.lessonNumber }
                                    ?.value
                                    .orEmpty()

                                val remainingLessons = state.day?.lessons?.first().orEmpty()
                                    .filter { lesson ->
                                        if (lesson in nextLessons && currentLessons.isEmpty()) return@filter false
                                        val lessonTimeItem = lesson.lessonTime.getFirstValueOld()!!
                                        lessonTimeItem.start > time.time
                                    }
                                    .sortedBySuspending { lesson ->
                                        val lessonTimeItem = lesson.lessonTime.getFirstValueOld()!!
                                        lessonTimeItem.start
                                    }
                                    .groupBy { it.lessonTime.getFirstValueOld()!!.lessonNumber }

                                state = state.copy(
                                    currentLessons = currentLessons,
                                    nextLessons = nextLessons,
                                    remainingLessons = remainingLessons
                                )
                                lastSpecialLessonUpdate = time
                            }
                            else {
                                state = state.copy(
                                    currentLessons = emptyList(),
                                    nextLessons = emptyList(),
                                    remainingLessons = state.day?.lessons?.first().orEmpty()
                                        .sortedBySuspending { it.lessonTime.getFirstValueOld()!!.lessonNumber }
                                        .groupBy { it.lessonTime.getFirstValueOld()!!.lessonNumber }
                                )
                            }
                        }
                }

                getDayUseCase(profile, state.currentTime.date)
                    .catch { e -> LOGGER.e { "Something went wrong on retrieving the day for Profile ${profile.id} (${profile.name}) at ${state.currentTime.date}:\n${e.stackTraceToString()}" } }
                    .collectLatest { day ->
                        state = state.copy(initDone = true)
                        if (day.lessons.first().any { it.lessonTime.getFirstValueOld()!!.end >= state.currentTime.time }) state = state.copy(
                            day = day
                        ) else if (day.nextSchoolDayId != null) App.daySource.getById(day.nextSchoolDayId, profile).filterIsInstance<CacheStateOld.Done<Day>>().map { it.data }.collectLatest { nextDay ->
                            state = state.copy(day = nextDay)
                        }
                    }
            }
        }
    }

    private fun update() {
        state = state.copy(isUpdating = true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                updateHolidaysUseCase(state.currentProfile!!.getSchool().getFirstValue() as School.Sp24School)
                updateTimetableUseCase(state.currentProfile!!.getSchool().getFirstValue() as School.Sp24School, forceUpdate = false)
                updateSubstitutionPlanUseCase(state.currentProfile!!.getSchool().getFirstValue() as School.Sp24School, listOfNotNull(state.day?.date, state.day?.nextSchoolDay?.getFirstValueOld()?.date), allowNotification = false)
            }
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
    val initDone: Boolean = false,
    val day: Day? = null,
    val isUpdating: Boolean = false,

    val news: List<News> = emptyList(),

    val currentLessons: List<CurrentLesson> = emptyList(),
    val nextLessons: List<Lesson> = emptyList(),
    val remainingLessons: Map<Int, List<Lesson>> = emptyMap()
)

sealed class HomeEvent {
    data object OnRefresh : HomeEvent()
}

data class CurrentLesson(
    val lesson: Lesson,
    val continuing: Lesson?
)