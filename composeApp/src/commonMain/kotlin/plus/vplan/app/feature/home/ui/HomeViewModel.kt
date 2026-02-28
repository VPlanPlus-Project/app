@file:OptIn(FlowPreview::class)

package plus.vplan.app.feature.home.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atDate
import kotlinx.datetime.atTime
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.core.model.News
import plus.vplan.app.core.model.Profile
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
import plus.vplan.app.domain.repository.Stundenplan24Repository
import plus.vplan.app.domain.usecase.GetCurrentDateTimeUseCase
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.domain.usecase.GetDayUseCase
import plus.vplan.app.feature.home.domain.usecase.GetNewsUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateHolidaysUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateLessonTimesUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateSubjectInstanceUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateTimetableUseCase
import plus.vplan.app.utils.now
import plus.vplan.app.utils.sortedBySuspending
import plus.vplan.lib.sp24.source.Authentication

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val getCurrentDateTimeUseCase: GetCurrentDateTimeUseCase,
    private val getDayUseCase: GetDayUseCase,
    private val getNewsUseCase: GetNewsUseCase,
    private val lessonPopulator: LessonPopulator,
    private val homeworkPopulator: HomeworkPopulator,
    private val assessmentPopulator: AssessmentPopulator,
    private val keyValueRepository: KeyValueRepository,
    private val stundenplan24Repository: Stundenplan24Repository,
    private val updateSubstitutionPlanUseCase: UpdateSubstitutionPlanUseCase,
    private val updateTimetableUseCase: UpdateTimetableUseCase,
    private val updateHolidaysUseCase: UpdateHolidaysUseCase,
    private val updateLessonTimesUseCase: UpdateLessonTimesUseCase,
    private val updateSubjectInstanceUseCase: UpdateSubjectInstanceUseCase,
) : ViewModel() {
    val state: StateFlow<HomeState>
        field = MutableStateFlow(HomeState())

    private val logger = Logger.withTag("HomeViewModel")

    private var newsJob: Job? = null

    init {
        // 1. Profile & News Setup
        viewModelScope.launch {
            getCurrentProfileUseCase()
                .filterNotNull()
                .collect { profile ->
                    state.update { it.copy(currentProfile = profile, initDone = true) }
                    launchNews(profile)
                }
        }

        // 2. Continuous Time Updates
        viewModelScope.launch {
            getCurrentDateTimeUseCase()
                .collect { time ->
                    state.update { it.copy(currentTime = time) }
                }
        }

        // 3. Main Data Orchestration
        viewModelScope.launch {
            val dayFlow = combine(
                state.map { it.currentProfile }.distinctUntilChanged(),
                state.map { it.currentTime.date }.distinctUntilChanged()
            ) { profile, date -> profile to date }
                .filter { (profile, _) -> profile != null }
                .flatMapLatest { (profile, date) ->
                    getDay(
                        profile = profile!!,
                        forDate = date,
                        shouldRetryRecursively = true
                    )
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

            combine(
                dayFlow,
                state.map { it.currentTime }.distinctUntilChanged()
            ) { dayWithDetails, time -> dayWithDetails to time }
                .distinctUntilChangedBy { (dayWithDetails, time) ->
                    val lessonIds = dayWithDetails?.lessons?.map { it.lesson.id }?.sorted()
                    "$lessonIds|${time.date}|${time.hour}|${time.minute}"
                }
                .collectLatest { (dayWithDetails, time) ->
                    logger.d { "Day With Details: date=${dayWithDetails?.populatedDay?.day?.date}, lessons=${dayWithDetails?.lessons?.size}, homework=${dayWithDetails?.homework?.size}, assessments=${dayWithDetails?.assessments?.size}" }
                    if (dayWithDetails == null) {
                        state.update { it.copy(day = null, currentLessons = emptyList(), nextLessons = emptyList(), remainingLessons = emptyMap()) }
                        return@collectLatest
                    }

                    val forceStatic = keyValueRepository.getBooleanOrDefault(
                        Keys.forceStaticTimetableHomescreen.key,
                        Keys.forceStaticTimetableHomescreen.default
                    ).first()

                    val canShowCurrentAndNext = !forceStatic && dayWithDetails.populatedDay.day.date == time.date
                    val lessons = dayWithDetails.lessons

                    val currentLessons = if (canShowCurrentAndNext) {
                        lessons.filter { lesson ->
                            val timeRange = lesson.lessonTime ?: return@filter false
                            time in timeRange.start.atDate(dayWithDetails.populatedDay.day.date)..timeRange.end.atDate(dayWithDetails.populatedDay.day.date)
                        }.map { lesson ->
                            CurrentLesson(
                                lesson = lesson,
                                continuing = lessons.firstOrNull {
                                    it.lesson.subject != null &&
                                            it.lesson.subject == lesson.lesson.subject &&
                                            (it.lesson as? Lesson.SubstitutionPlanLesson)?.subjectInstanceId ==
                                            (lesson.lesson as? Lesson.SubstitutionPlanLesson)?.subjectInstanceId &&
                                            it.lesson.lessonNumber == lesson.lesson.lessonNumber + 1
                                }
                            )
                        }.sortedBySuspending {
                            val subjectInstance = (it.lesson as? PopulatedLesson.SubstitutionPlanLesson)?.subjectInstance
                            it.lesson.lesson.subject + (subjectInstance?.course?.name ?: "")
                        }
                    } else emptyList()

                    val nextLessons = if (canShowCurrentAndNext) {
                        lessons.filter { lesson ->
                            val timeRange = lesson.lessonTime ?: return@filter true
                            timeRange.start.atDate(dayWithDetails.populatedDay.day.date) > time
                        }
                            .groupBy { it.lesson.lessonNumber }
                            .minByOrNull { it.key }?.value.orEmpty()
                    } else emptyList()

                    val remainingLessons = lessons
                        .filter { lesson ->
                            if (!canShowCurrentAndNext || (nextLessons.isEmpty() && currentLessons.isEmpty())) return@filter true
                            if (lesson in nextLessons && currentLessons.isEmpty()) return@filter false
                            val timeRange = lesson.lessonTime ?: return@filter true
                            timeRange.start.atDate(dayWithDetails.populatedDay.day.date) > time
                        }
                        .sortedBySuspending { lesson ->
                            val subject = lesson.lesson.subject ?: ""
                            val subjectInstance = (lesson as? PopulatedLesson.SubstitutionPlanLesson)?.subjectInstance
                            val courseName = subjectInstance?.course?.name ?: ""
                            lesson.lesson.lessonNumber.toString().padStart(2, '0') + "${subject}_${courseName}"
                        }
                        .groupBy { it.lesson.lessonNumber }

                    state.update {
                        it.copy(
                            day = dayWithDetails,
                            currentLessons = currentLessons,
                            nextLessons = nextLessons,
                            remainingLessons = remainingLessons,
                            hasInterpolatedLessonTimes = lessons.any { l -> l.lessonTime?.interpolated == true }
                        )
                    }
                }
        }
    }

    private fun launchNews(profile: Profile) {
        newsJob?.cancel()
        newsJob = viewModelScope.launch {
            getNewsUseCase(profile).collect { news ->
                state.update { it.copy(news = news) }
            }
        }
    }

    private fun getDay(profile: Profile, forDate: LocalDate, shouldRetryRecursively: Boolean): Flow<DayWithDetails?> {
        logger.d { "[$forDate] getDay called (recursive=$shouldRetryRecursively)" }
        return getDayUseCase(profile, forDate)
            .distinctUntilChangedBy { day ->
                val lessonIds = (day.substitution.ifEmpty { day.timetable }).map { it.id }.sorted()
                "${day.day.id}|${day.day.info}|${day.day.dayType}|${day.holiday?.id}|$lessonIds"
            }
            .flatMapLatest { day ->
                logger.d { "[$forDate] getDay flatMapLatest → timetable=${day.timetable.size} substitution=${day.substitution.size} – calling populateMultiple" }
                combine(
                    homeworkPopulator.populateMultiple(day.homework, PopulationContext.Profile(profile)),
                    assessmentPopulator.populateMultiple(day.assessments, PopulationContext.Profile(profile)),
                    lessonPopulator.populateMultiple(day.substitution.ifEmpty { day.timetable }, PopulationContext.Profile(profile)),
                ) { homework, assessments, lessons ->
                    logger.d { "[$forDate] populateMultiple combine fired → populatedLessons=${lessons.size}" }
                    DayWithDetails(day, lessons, homework, assessments)
                }.distinctUntilChangedBy { d ->
                    val lessonIds = d.lessons.map { it.lesson.id }.sorted()
                    val homeworkIds = d.homework.map { it.homework.id }.sorted()
                    val assessmentIds = d.assessments.map { it.assessment.id }.sorted()
                    "$lessonIds|$homeworkIds|$assessmentIds"
                }.flatMapLatest { dayWithDetails ->
                    // Retry with next school day if current day is completed
                    if (shouldRetryRecursively && dayWithDetails.isCompleted(LocalDateTime.now()) && dayWithDetails.populatedDay.day.nextSchoolDay != null) {
                        logger.d { "[$forDate] day completed → retrying with nextSchoolDay=${dayWithDetails.populatedDay.day.nextSchoolDay}" }
                        getDay(profile, dayWithDetails.populatedDay.day.nextSchoolDay!!, false)
                    } else {
                        flowOf(dayWithDetails)
                    }
                }
            }
    }

    fun onEvent(event: HomeEvent) {
        viewModelScope.launch {
            when (event) {
                is HomeEvent.OnRefresh -> {
                    val profile = state.value.currentProfile ?: return@launch
                    val school = profile.school
                    state.update { it.copy(isUpdating = true) }
                    try {
                        coroutineScope {
                            val client = stundenplan24Repository.getSp24Client(
                                Authentication(school.sp24Id, school.username, school.password),
                                true
                            )
                            updateSubjectInstanceUseCase(school, client)
                            updateLessonTimesUseCase(school, client)
                            updateHolidaysUseCase(school, client)
                            updateTimetableUseCase(school, forceUpdate = false, client = client)
                            updateSubstitutionPlanUseCase(
                                school,
                                setOfNotNull(
                                    LocalDate.now(),
                                    state.value.day?.populatedDay?.day?.date,
                                    state.value.day?.populatedDay?.day?.nextSchoolDay
                                ).sorted(),
                                allowNotification = false,
                                providedClient = client
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        state.update { it.copy(isUpdating = false) }
                    }
                }
            }
        }
    }
}

@Immutable
data class HomeState(
    val currentProfile: Profile? = null,
    val currentTime: LocalDateTime = LocalDateTime.now(),
    val initDone: Boolean = false,
    val day: DayWithDetails? = null,
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

data class DayWithDetails(
    val populatedDay: PopulatedDay,
    val lessons: List<PopulatedLesson>,
    val homework: List<PopulatedHomework>,
    val assessments: List<PopulatedAssessment>
) {
    fun isCompleted(comparedTo: LocalDateTime): Boolean {
        if (lessons.isEmpty()) return true
        return lessons.all { lesson ->
            val lessonTime = lesson.lessonTime ?: return@all true
            val plannedEnd = comparedTo.date.atTime(lessonTime.end)
            plannedEnd < comparedTo
        }
    }
}

data class CurrentLesson(
    val lesson: PopulatedLesson,
    val continuing: PopulatedLesson?
)