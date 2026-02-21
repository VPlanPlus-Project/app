@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.feature.grades.page.analytics.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.data.besteschule.IntervalsRepository
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.besteschule.BesteSchuleGrade
import plus.vplan.app.core.model.besteschule.BesteSchuleSubject
import plus.vplan.app.domain.model.populated.besteschule.GradesPopulator
import plus.vplan.app.domain.model.populated.besteschule.IntervalPopulator
import plus.vplan.app.domain.model.populated.besteschule.PopulatedGrade
import plus.vplan.app.domain.model.populated.besteschule.PopulatedInterval
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.domain.repository.besteschule.BesteSchuleGradesRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleSubjectsRepository
import plus.vplan.app.utils.now

class AnalyticsViewModel(
    private val vppIdRepository: VppIdRepository
) : ViewModel(), KoinComponent {
    var state by mutableStateOf(AnalyticsState())
        private set

    private val besteSchuleGradesRepository by inject<BesteSchuleGradesRepository>()
    private val besteSchuleIntervalsRepository by inject<IntervalsRepository>()
    private val besteSchuleSubjectsRepository by inject<BesteSchuleSubjectsRepository>()

    private val gradesPopulator by inject<GradesPopulator>()
    private val intervalPopulator by inject<IntervalPopulator>()

    private var mainJob: Job? = null
    fun init(vppIdId: Int) {
        mainJob?.cancel()
        state = AnalyticsState()
        mainJob = viewModelScope.launch {
            val activeJobs = mutableListOf<Job>()
            vppIdRepository.getById(vppIdId, ResponsePreference.Fast)
                .filterIsInstance<CacheState.Done<VppId.Active>>()
                .map { it.data }
                .filter { it.schulverwalterConnection != null }
                .distinctUntilChangedBy { it.id.hashCode() + it.schulverwalterConnection.hashCode() }
                .onEach { vppIdActive ->
                    activeJobs.forEach { it.cancelAndJoin() }
                    activeJobs.clear()
                    state = state.copy(vppId = vppIdActive)
                }
                .collectLatest { vppId ->
                    launch {
                        besteSchuleIntervalsRepository.getAll()
                            .flatMapLatest { intervalPopulator.populateMultiple(it) }
                            .collectLatest { intervals ->
                                state = state.copy(
                                    intervals = intervals,
                                    interval = if (state.intervals.isEmpty()) intervals.firstOrNull { LocalDate.now() in it.interval.from..it.interval.to } else state.interval
                                )
                            }
                    }.let(activeJobs::add)

                    launch {
                        besteSchuleGradesRepository.getGrades(
                            responsePreference = ResponsePreference.Fast,
                            contextBesteschuleAccessToken = vppId.schulverwalterConnection!!.accessToken,
                            contextBesteschuleUserId = vppId.schulverwalterConnection!!.userId
                        )
                            .filterIsInstance<Response.Success<List<BesteSchuleGrade>>>()
                            .map { it.data }
                            .flatMapLatest { grades -> gradesPopulator.populateMultiple(grades) }
                            .collectLatest { grades ->
                                val subjects = besteSchuleSubjectsRepository.getAllFromCache().first()
                                state = state.copy(
                                    grades = grades,
                                    filteredGrades = emptyList(),
                                    availableSubjectFilters = grades
                                        .map { it.collection.subjectId }
                                        .distinct()
                                        .let { subjects.filter { subject -> subject.id in it } }
                                        .sortedBy { it.shortName },
                                    filteredSubjects = emptyList()
                                )
                                updateFiltered()
                            }
                    }.let(activeJobs::add)
                }
        }
    }

    fun onEvent(event: AnalyticsAction) {
        viewModelScope.launch {
            when (event) {
                is AnalyticsAction.ToggleSubjectFilter -> {
                    val add = state.filteredSubjects.none { it.id == event.subject.id }
                    state = if (add) state.copy(filteredSubjects = state.filteredSubjects + event.subject)
                    else state.copy(filteredSubjects = state.filteredSubjects.filter { it.id != event.subject.id })
                    updateFiltered()
                }
                is AnalyticsAction.SetInterval -> {
                    state = state.copy(interval = event.interval)
                    updateFiltered()
                }
            }
        }
    }

    private fun updateFiltered() {
        state = state.copy(filteredGrades = state.grades
            .filter { grade ->
                state.filteredSubjects.any { subject -> grade.collection.subjectId == subject.id } || state.filteredSubjects.isEmpty()
            }
            .filter { it.collection.intervalId in listOfNotNull(state.interval?.interval?.id, state.interval?.includedInterval?.id) }
        )
    }
}

data class AnalyticsState(
    val vppId: VppId? = null,
    val interval: PopulatedInterval? = null,
    val intervals: List<PopulatedInterval> = emptyList(),
    val grades: List<PopulatedGrade> = emptyList(),
    val filteredGrades: List<PopulatedGrade> = emptyList(),

    val availableSubjectFilters: List<BesteSchuleSubject> = emptyList(),
    val filteredSubjects: List<BesteSchuleSubject> = emptyList(),
)

sealed class AnalyticsAction {
    data class ToggleSubjectFilter(val subject: BesteSchuleSubject) : AnalyticsAction()

    data class SetInterval(val interval: PopulatedInterval) : AnalyticsAction()
}
