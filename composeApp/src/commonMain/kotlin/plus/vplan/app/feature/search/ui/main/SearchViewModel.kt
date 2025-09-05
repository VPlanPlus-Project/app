@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.feature.search.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.App
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetCurrentDateTimeUseCase
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.grades.domain.usecase.GetGradeLockStateUseCase
import plus.vplan.app.feature.grades.domain.usecase.GradeLockState
import plus.vplan.app.feature.grades.domain.usecase.LockGradesUseCase
import plus.vplan.app.feature.grades.domain.usecase.RequestGradeUnlockUseCase
import plus.vplan.app.feature.search.domain.model.SearchResult
import plus.vplan.app.feature.search.domain.usecase.GetAssessmentsForProfileUseCase
import plus.vplan.app.feature.search.domain.usecase.GetHomeworkForProfileUseCase
import plus.vplan.app.feature.search.domain.usecase.GetSubjectsUseCase
import plus.vplan.app.feature.search.domain.usecase.SearchRequest
import plus.vplan.app.feature.search.domain.usecase.SearchUseCase
import plus.vplan.app.utils.now
import kotlin.time.ExperimentalTime

class SearchViewModel(
    private val searchUseCase: SearchUseCase,
    private val getCurrentDateTimeUseCase: GetCurrentDateTimeUseCase,
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val getHomeworkForProfileUseCase: GetHomeworkForProfileUseCase,
    private val getAssessmentsForProfileUseCase: GetAssessmentsForProfileUseCase,
    private val getGradeLockStateUseCase: GetGradeLockStateUseCase,
    private val lockGradesUseCase: LockGradesUseCase,
    private val requestGradeUnlockUseCase: RequestGradeUnlockUseCase,
    private val getSubjectsUseCase: GetSubjectsUseCase
) : ViewModel() {

    var state by mutableStateOf(SearchState())
        private set

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            getCurrentDateTimeUseCase().collectLatest { state = state.copy(currentTime = it) }
        }

        viewModelScope.launch {
            getGradeLockStateUseCase().collectLatest { state = state.copy(gradeLockState = it) }
        }

        viewModelScope.launch {
            var homeworkJob: Job? = null
            var assessmentJob: Job? = null
            getCurrentProfileUseCase().collectLatest { currentProfile ->
                state = state.copy(currentProfile = currentProfile)
                homeworkJob?.cancel()
                assessmentJob?.cancel()
                if (currentProfile is Profile.StudentProfile) {
                    homeworkJob = launch { getHomeworkForProfileUseCase(currentProfile).collectLatest { state = state.copy(homework = it) } }
                    assessmentJob = launch { getAssessmentsForProfileUseCase(currentProfile).collectLatest { state = state.copy(assessments = it) } }
                }
                getSubjectsUseCase(currentProfile).let { state = state.copy(subjects = it) }
            }
        }
    }

    fun onEvent(event: SearchEvent) {
        viewModelScope.launch {
            when (event) {
                is SearchEvent.UpdateQuery -> {
                    state = state.copy(query = state.query.copy(query = event.query))
                    restartSearch()
                }
                is SearchEvent.SelectDate -> {
                    state = state.copy(query = state.query.copy(date = event.date))
                    restartSearch()
                }
                is SearchEvent.FilterForSubject -> {
                    state = state.copy(query = state.query.copy(subject = event.subject))
                    restartSearch()
                }
                is SearchEvent.FilterForAssessmentType -> {
                    state = state.copy(query = state.query.copy(assessmentType = event.type))
                    restartSearch()
                }
                is SearchEvent.RequestGradeLock -> lockGradesUseCase()
                is SearchEvent.RequestGradeUnlock -> requestGradeUnlockUseCase()
                is SearchEvent.SearchBarFocusChanged -> {
                    if (state.isSearchBarFocused == event.isFocused) return@launch
                    state = state.copy(isSearchBarFocused = event.isFocused)
                }
            }
        }
    }

    private suspend fun restartSearch() {
        searchJob?.cancel()
        val schoolId = state.currentProfile?.getSchool()?.getFirstValue()?.id
        if (state.currentProfile == null || schoolId == null) {
            state = state.copy(results = emptyMap())
            return
        }
        val day = App.daySource.getById(Day.buildId(schoolId, state.query.date)).getFirstValueOld()
        searchJob = viewModelScope.launch {
            state = state.copy(isLoading = true)
            searchUseCase(state.query).collectLatest {
                state = state.copy(results = it, selectedDateType = day?.dayType ?: Day.DayType.UNKNOWN, isLoading = false)
            }
        }
    }
}

data class SearchState(
    val query: SearchRequest = SearchRequest(),
    val isLoading: Boolean = false,
    val selectedDateType: Day.DayType = Day.DayType.UNKNOWN,
    val results: Map<SearchResult.Type, List<SearchResult>> = emptyMap(),
    val homework: List<Homework> = emptyList(),
    val assessments: List<Assessment> = emptyList(),
    val currentProfile: Profile? = null,
    val currentTime: LocalDateTime = LocalDateTime.now(),
    val gradeLockState: GradeLockState = GradeLockState.NotConfigured,
    val subjects: List<String> = emptyList(),
    val isSearchBarFocused: Boolean = false
) {
    val newItems = (assessments.map { NewItem.Assessment(it) } + homework.map { NewItem.Homework(it) }).sortedByDescending { it.createdAt }.take(5)
}

sealed class SearchEvent {
    data class UpdateQuery(val query: String): SearchEvent()
    data class SelectDate(val date: LocalDate): SearchEvent()
    data class FilterForSubject(val subject: String?): SearchEvent()
    data class FilterForAssessmentType(val type: Assessment.Type?): SearchEvent()
    data class SearchBarFocusChanged(val isFocused: Boolean): SearchEvent()

    data object RequestGradeUnlock: SearchEvent()
    data object RequestGradeLock: SearchEvent()
}

sealed class NewItem(val createdAt: LocalDate) {
    data class Assessment(val assessment: plus.vplan.app.domain.model.Assessment): NewItem(assessment.createdAt.date)
    data class Homework(val homework: plus.vplan.app.domain.model.Homework): NewItem(homework.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date)
}