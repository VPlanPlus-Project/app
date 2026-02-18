package plus.vplan.app.feature.home.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.App
import plus.vplan.app.captureError
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.News
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.getFirstValueOld
import plus.vplan.app.domain.model.Day
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.domain.model.populated.LessonPopulator
import plus.vplan.app.domain.model.populated.PopulatedLesson
import plus.vplan.app.domain.model.populated.PopulationContext
import plus.vplan.app.domain.model.populated.SubjectInstancePopulator
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.Stundenplan24Repository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
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

private val LOGGER = Logger.withTag("HomeViewModel")

@OptIn(FlowPreview::class)
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
    private val keyValueRepository: KeyValueRepository,
    private val subjectInstancePopulator: SubjectInstancePopulator,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val lessonPopulator: LessonPopulator,
) : ViewModel() {
    val state: StateFlow<HomeState>
        field = MutableStateFlow(HomeState())

    init {
        viewModelScope.launch {
            var newsJob: Job? = null
            var specialLessonsUpdateJob: Job? = null
            getCurrentProfileUseCase().collectLatest { profile ->
                LOGGER.d { "Current Profile: $profile" }
                state.update { state ->
                    state.copy(
                        currentProfile = profile,
                        day = null,
                        initDone = false
                    )
                }
                newsJob?.cancel()
                newsJob = launch { getNewsUseCase(profile).collectLatest {
                    state.update { state -> state.copy(news = it) }
                } }

                specialLessonsUpdateJob?.cancel()
                specialLessonsUpdateJob = launch {
                    var lastSpecialLessonUpdate = LocalDateTime.now() - 1.hours
                    getCurrentDateTimeUseCase()
                        .onEach { time -> state.update { state -> state.copy(currentTime = time) } }
                        .collect { time ->
                            if (lastSpecialLessonUpdate until time < 5.seconds) return@collect

                            val hasInterpolatedLessonTimes = state.value.day?.lessons?.first().orEmpty().toList()
                                .let { lessonPopulator.populateMultiple(it, PopulationContext.Profile(state.value.currentProfile!!)).first() }
                                .any { lesson -> lesson.lessonTime?.interpolated == true }

                            if (state.value.day?.date == time.date) {
                                val allLessons = state.value.day?.lessons?.first().orEmpty().toList()
                                    .let { lessonPopulator.populateMultiple(it, PopulationContext.Profile(state.value.currentProfile!!)).first() }

                                /**
                                 * If the current or next lesson can be determined reliably, show them. Otherwise, only show the full list of lessons.
                                 * This includes the corresponding developer setting.
                                 */
                                val canShowCurrentAndNextLesson = !keyValueRepository.getBooleanOrDefault(Keys.forceStaticTimetableHomescreen.key, Keys.forceStaticTimetableHomescreen.default).first() &&
                                        (allLessons.isEmpty() || allLessons.count { it.lessonTime?.interpolated != false } <= allLessons.size)

                                val currentLessons = if (!canShowCurrentAndNextLesson) null else allLessons
                                    .filter { lesson ->
                                        val lessonTimeItem = lesson.lessonTime ?: return@filter false
                                        time.time in lessonTimeItem.start..lessonTimeItem.end
                                    }.map { lesson ->
                                        CurrentLesson(
                                            lesson = lesson,
                                            continuing = allLessons.firstOrNull {
                                                it.lesson.subject != null && it.lesson.subject == lesson.lesson.subject && (it.lesson as? Lesson.SubstitutionPlanLesson)?.subjectInstanceId == (lesson.lesson as? Lesson.SubstitutionPlanLesson)?.subjectInstanceId && it.lesson.lessonNumber == lesson.lesson.lessonNumber + 1
                                            }
                                        )
                                    }
                                    .sortedBySuspending {
                                        val subjectInstance = (it.lesson.lesson as? Lesson.SubstitutionPlanLesson)?.subjectInstanceId
                                            ?.let { id -> subjectInstanceRepository.getByLocalId(id).first() }
                                            ?.let { subjectInstance -> subjectInstancePopulator.populateSingle(subjectInstance).first() }
                                        it.lesson.lesson.subject + subjectInstance?.course?.name
                                    }

                                val nextLessons = if (!canShowCurrentAndNextLesson) null else allLessons
                                    .filter { lesson ->
                                        val lessonTimeItem = lesson.lessonTime ?: return@filter true
                                        lessonTimeItem.start > time.time
                                    }
                                    .groupBy { it.lesson.lessonNumber }
                                    .minByOrNull { it.key }
                                    ?.value
                                    .orEmpty()

                                val remainingLessons = allLessons
                                    .filter { lesson ->
                                        if (!canShowCurrentAndNextLesson || nextLessons == null || currentLessons == null) return@filter true // Show all lessons if current/next cannot be determined
                                        if (lesson in nextLessons && currentLessons.isEmpty()) return@filter false
                                        val lessonTimeItem = lesson.lessonTime ?: return@filter true
                                        lessonTimeItem.start > time.time
                                    }
                                    .sortedBySuspending { lesson ->
                                        val subject = lesson.lesson.subject ?: ""
                                        val subjectInstance = (lesson.lesson as? Lesson.SubstitutionPlanLesson)?.subjectInstanceId
                                            ?.let { subjectInstanceId -> subjectInstanceRepository.getByLocalId(subjectInstanceId).first() }
                                            ?.let { subjectInstancePopulator.populateSingle(it).first() }
                                        val courseName = subjectInstance?.course?.name ?: ""
                                        lesson.lesson.lessonNumber.toString().padStart(2, '0') + "${subject}_${courseName}"
                                    }
                                    .groupBy { it.lesson.lessonNumber }

                                state.update { state ->
                                    state.copy(
                                        currentLessons = currentLessons.orEmpty(),
                                        nextLessons = nextLessons.orEmpty(),
                                        remainingLessons = remainingLessons,
                                        hasInterpolatedLessonTimes = hasInterpolatedLessonTimes
                                    )
                                }
                                lastSpecialLessonUpdate = time
                            } else {
                                state.update { state ->
                                    state.copy(
                                        currentLessons = emptyList(),
                                        nextLessons = emptyList(),
                                        remainingLessons = state.day?.lessons?.first().orEmpty().toList()
                                            .let { lessonPopulator.populateMultiple(it, PopulationContext.Profile(state.currentProfile!!)).first() }
                                            .sortedBySuspending { lesson ->
                                                val subject = lesson.lesson.subject ?: ""
                                                val subjectInstance = (lesson.lesson as? Lesson.SubstitutionPlanLesson)?.subjectInstanceId
                                                    ?.let { subjectInstanceId -> subjectInstanceRepository.getByLocalId(subjectInstanceId).first() }
                                                    ?.let { subjectInstancePopulator.populateSingle(it).first() }
                                                val courseName = subjectInstance?.course?.name ?: ""
                                                lesson.lesson.lessonNumber.toString().padStart(2, '0') + "${subject}_${courseName}"
                                            }
                                            .groupBy { it.lesson.lessonNumber },
                                        hasInterpolatedLessonTimes = hasInterpolatedLessonTimes
                                    )
                                }
                            }
                        }
                }

                getDayUseCase(profile, state.value.currentTime.date)
                    .catch { e -> LOGGER.e { "Something went wrong on retrieving the day for Profile ${profile.id} (${profile.name}) at ${state.value.currentTime.date}:\n${e.stackTraceToString()}" } }
                    .collectLatest { day ->
                        state.update { state -> state.copy(initDone = true) }

                        val hasDayMissingLessonTimes = day.lessons.first().any { it.lessonTimeId == null }
                        if (hasDayMissingLessonTimes) state.update { state -> state.copy(day = day) }

                        val lessons = day.lessons.first().toList()
                            .let { lessonPopulator.populateMultiple(it, PopulationContext.Profile(profile)).first() }

                        if (lessons.filter { it.lessonTime != null }.any { it.lessonTime!!.end >= state.value.currentTime.time }) {
                            state.update { state -> state.copy(day = day) }
                        } else if (day.nextSchoolDayId != null) {
                            App.daySource.getById(
                                day.nextSchoolDayId,
                                profile
                            ).filterIsInstance<CacheState.Done<Day>>().map { it.data }
                                .collectLatest { nextDay ->
                                    state.update { state -> state.copy(day = nextDay) }
                                }
                        }
                    }
            }
        }
    }

    private fun update() {
        state.update { state -> state.copy(isUpdating = true) }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val school = state.value.currentProfile!!.school
                try {
                    val client = stundenplan24Repository.getSp24Client(Authentication(school.sp24Id, school.username, school.password), true)
                    updateSubjectInstanceUseCase(school, client)
                    updateLessonTimesUseCase(school, client)
                    updateHolidaysUseCase(school, client)
                    updateTimetableUseCase(school, forceUpdate = false, client = client)
                    updateSubstitutionPlanUseCase(school, setOfNotNull(LocalDate.now(), state.value.day?.date, state.value.day?.nextSchoolDay?.getFirstValueOld()?.date).sorted(), allowNotification = false, providedClient = client)
                } catch (e: Exception) {
                    LOGGER.e { "Something went wrong on updating the data for Profile ${state.value.currentProfile!!.id} (${state.value.currentProfile!!.name}):\n${e.stackTraceToString()}" }
                    captureError("HomeViewModel.update", "Error on updating the data for school ${school.id}: ${e.stackTraceToString()}")
                }
            }
        }.invokeOnCompletion {
            state.update { state ->
                state.copy(isUpdating = false)
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        viewModelScope.launch {
            when (event) {
                HomeEvent.OnRefresh -> update()
            }
        }
    }
}

@Immutable
data class HomeState(
    val currentProfile: Profile? = null,
    val currentTime: LocalDateTime = LocalDateTime.now(),
    val initDone: Boolean = false,
    val day: Day? = null,
    val isUpdating: Boolean = false,

    val news: List<News> = emptyList(),

    val hasInterpolatedLessonTimes: Boolean = false,
    val currentLessons: List<CurrentLesson> = emptyList(),
    val nextLessons: List<PopulatedLesson> = emptyList(),
    val remainingLessons: Map<Int, List<PopulatedLesson>> = emptyMap()
)

sealed class HomeEvent {
    data object OnRefresh : HomeEvent()
}

data class CurrentLesson(
    val lesson: PopulatedLesson,
    val continuing: PopulatedLesson?
)