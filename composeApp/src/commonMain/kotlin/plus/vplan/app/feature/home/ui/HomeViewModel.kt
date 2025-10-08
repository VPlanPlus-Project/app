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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.App
import plus.vplan.app.captureError
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.News
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.Stundenplan24Repository
import plus.vplan.app.domain.usecase.GetCurrentDateTimeUseCase
import plus.vplan.app.domain.usecase.GetDayUseCase
import plus.vplan.app.feature.home.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.home.domain.usecase.GetNewsUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateHolidaysUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateLessonTimesUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateSubjectInstanceUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateTimetableUseCase
import plus.vplan.app.utils.minus
import plus.vplan.app.utils.now
import plus.vplan.app.utils.sortedBySuspending
import plus.vplan.app.utils.until
import plus.vplan.lib.sp24.source.Authentication
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
    private val updateLessonTimesUseCase: UpdateLessonTimesUseCase,
    private val updateSubjectInstanceUseCase: UpdateSubjectInstanceUseCase,
    private val getNewsUseCase: GetNewsUseCase,
    private val stundenplan24Repository: Stundenplan24Repository,
    private val keyValueRepository: KeyValueRepository
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

                            val hasInterpolatedLessonTimes = state.day?.lessons?.first().orEmpty()
                                .any { lesson -> lesson.lessonTime?.getFirstValueOld()?.interpolated == true }

                            if (state.day?.date == time.date) {
                                val allLessons = state.day?.lessons?.first().orEmpty()

                                /**
                                 * If the current or next lesson can be determined reliably, show them. Otherwise, only show the full list of lessons.
                                 * This includes the corresponding developer setting.
                                 */
                                val canShowCurrentAndNextLesson = !keyValueRepository.getBooleanOrDefault(Keys.forceStaticTimetableHomescreen.key, Keys.forceStaticTimetableHomescreen.default).first() &&
                                        (allLessons.isEmpty() || allLessons.count { it.lessonTime?.getFirstValueOld()?.interpolated != false } <= allLessons.size)

                                val currentLessons = if (!canShowCurrentAndNextLesson) null else allLessons
                                    .filter { lesson ->
                                        val lessonTimeItem = lesson.lessonTime?.getFirstValueOld() ?: return@filter false
                                        time.time in lessonTimeItem.start..lessonTimeItem.end
                                    }.map { lesson ->
                                        CurrentLesson(
                                            lesson = lesson,
                                            continuing = allLessons.firstOrNull {
                                                it.subject != null && it.subject == lesson.subject && it.subjectInstanceId == lesson.subjectInstanceId && it.lessonNumber == lesson.lessonNumber + 1
                                            }
                                        )
                                    }
                                    .sortedBySuspending { it.lesson.subject + it.lesson.subjectInstance?.getFirstValue()?.course?.getFirstValue()?.name }

                                val nextLessons = if (!canShowCurrentAndNextLesson) null else allLessons
                                    .filter { lesson ->
                                        val lessonTimeItem = lesson.lessonTime?.getFirstValueOld() ?: return@filter true
                                        lessonTimeItem.start > time.time
                                    }
                                    .groupBy { it.lessonNumber }
                                    .minByOrNull { it.key }
                                    ?.value
                                    .orEmpty()

                                val remainingLessons = allLessons
                                    .filter { lesson ->
                                        if (!canShowCurrentAndNextLesson || nextLessons == null || currentLessons == null) return@filter true // Show all lessons if current/next cannot be determined
                                        if (lesson in nextLessons && currentLessons.isEmpty()) return@filter false
                                        val lessonTimeItem = lesson.lessonTime?.getFirstValueOld() ?: return@filter true
                                        lessonTimeItem.start > time.time
                                    }
                                    .sortedBySuspending { lesson ->
                                        val subject = lesson.subject ?: ""
                                        val courseName = lesson.subjectInstance?.getFirstValue()?.course?.getFirstValue()?.name ?: ""
                                        lesson.lessonNumber.toString().padStart(2, '0') + "${subject}_${courseName}"
                                    }
                                    .groupBy { it.lessonNumber }

                                state = state.copy(
                                    currentLessons = currentLessons.orEmpty(),
                                    nextLessons = nextLessons.orEmpty(),
                                    remainingLessons = remainingLessons,
                                    hasInterpolatedLessonTimes = hasInterpolatedLessonTimes
                                )
                                lastSpecialLessonUpdate = time
                            } else {
                                state = state.copy(
                                    currentLessons = emptyList(),
                                    nextLessons = emptyList(),
                                    remainingLessons = state.day?.lessons?.first().orEmpty()
                                        .sortedBySuspending { lesson ->
                                            val subject = lesson.subject ?: ""
                                            val courseName = lesson.subjectInstance?.getFirstValue()?.course?.getFirstValue()?.name ?: ""
                                            lesson.lessonNumber.toString().padStart(2, '0') + "${subject}_${courseName}"
                                        }
                                        .groupBy { it.lessonNumber },
                                    hasInterpolatedLessonTimes = hasInterpolatedLessonTimes
                                )
                            }
                        }
                }

                getDayUseCase(profile, state.currentTime.date)
                    .catch { e -> LOGGER.e { "Something went wrong on retrieving the day for Profile ${profile.id} (${profile.name}) at ${state.currentTime.date}:\n${e.stackTraceToString()}" } }
                    .collectLatest { day ->
                        state = state.copy(initDone = true)

                        val hasDayMissingLessonTimes = day.lessons.first().any { it.lessonTime == null }
                        if (hasDayMissingLessonTimes) state = state.copy(day = day)

                        if (day.lessons.first().any { it.lessonTime!!.getFirstValueOld()!!.end >= state.currentTime.time }) state = state.copy(
                            day = day
                        ) else if (day.nextSchoolDayId != null) App.daySource.getById(day.nextSchoolDayId, profile).filterIsInstance<CacheState.Done<Day>>().map { it.data }.collectLatest { nextDay ->
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
                val school = state.currentProfile!!.getSchool().getFirstValue() as School.AppSchool
                try {
                    val client = stundenplan24Repository.getSp24Client(Authentication(school.sp24Id, school.username, school.password), true)
                    updateSubjectInstanceUseCase(school, client)
                    updateLessonTimesUseCase(school, client)
                    updateHolidaysUseCase(state.currentProfile!!.getSchool().getFirstValue() as School.AppSchool, client)
                    updateTimetableUseCase(state.currentProfile!!.getSchool().getFirstValue() as School.AppSchool, forceUpdate = false, client = client)
                    updateSubstitutionPlanUseCase(state.currentProfile!!.getSchool().getFirstValue() as School.AppSchool, setOfNotNull(LocalDate.now(), state.day?.date, state.day?.nextSchoolDay?.getFirstValueOld()?.date).sorted(), allowNotification = false, providedClient = client)
                } catch (e: Exception) {
                    LOGGER.e { "Something went wrong on updating the data for Profile ${state.currentProfile!!.id} (${state.currentProfile!!.name}):\n${e.stackTraceToString()}" }
                    captureError("HomeViewModel.update", "Error on updating the data for school ${school.id}: ${e.stackTraceToString()}")
                }
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
    val currentTime: LocalDateTime = LocalDateTime.now(),
    val initDone: Boolean = false,
    val day: Day? = null,
    val isUpdating: Boolean = false,

    val news: List<News> = emptyList(),

    val hasInterpolatedLessonTimes: Boolean = false,
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