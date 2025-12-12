package plus.vplan.app.feature.grades.page.analytics.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import plus.vplan.app.domain.model.besteschule.BesteSchuleInterval
import plus.vplan.app.domain.model.besteschule.BesteSchuleSubject
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.domain.repository.besteschule.BesteSchuleGradesRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleIntervalsRepository
import plus.vplan.app.utils.now

class AnalyticsViewModel(
    private val vppIdRepository: VppIdRepository
) : ViewModel(), KoinComponent {
    var state by mutableStateOf(AnalyticsState())
        private set

    private val besteSchuleGradesRepository by inject<BesteSchuleGradesRepository>()
    private val besteSchuleIntervalsRepository by inject<BesteSchuleIntervalsRepository>()

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
                        besteSchuleIntervalsRepository.getIntervals(
                            responsePreference = ResponsePreference.Fast,
                            contextBesteschuleAccessToken = vppId.schulverwalterConnection!!.accessToken,
                            contextBesteschuleUserId = vppId.schulverwalterConnection.userId
                        )
                            .filterIsInstance<Response.Success<List<BesteSchuleInterval>>>()
                            .map { it.data }
                            .collectLatest { intervals ->
                                state = state.copy(
                                    intervals = intervals,
                                    interval = if (state.intervals.isEmpty()) intervals.firstOrNull { LocalDate.now() in it.from..it.to } else state.interval
                                )
                            }
                    }.let(activeJobs::add)

                    launch {
                        besteSchuleGradesRepository.getGrades(
                            responsePreference = ResponsePreference.Fast,
                            contextBesteschuleAccessToken = vppId.schulverwalterConnection!!.accessToken,
                            contextBesteschuleUserId = vppId.schulverwalterConnection.userId
                        )
                            .filterIsInstance<Response.Success<List<BesteSchuleGrade>>>()
                            .map { it.data }
                            .collectLatest { grades ->
                                state = state.copy(
                                    grades = grades,
                                    filteredGrades = emptyList(),
                                    availableSubjectFilters = grades.map { it.collection.first()!!.subject.first()!! }.distinctBy { subject -> subject.id }.sortedBy { it.shortName },
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

    private suspend fun updateFiltered() {
        state = state.copy(filteredGrades = state.grades
            .filter { grade ->
                state.filteredSubjects.any { subject -> grade.collection.first()!!.subjectId == subject.id } || state.filteredSubjects.isEmpty()
            }
            .filter { it.collection.first()!!.intervalId in listOfNotNull(state.interval?.id, state.interval?.includedIntervalId) }
        )
    }
}

data class AnalyticsState(
    val vppId: VppId? = null,
    val interval: BesteSchuleInterval? = null,
    val intervals: List<BesteSchuleInterval> = emptyList(),
    val grades: List<BesteSchuleGrade> = emptyList(),
    val filteredGrades: List<BesteSchuleGrade> = emptyList(),

    val availableSubjectFilters: List<BesteSchuleSubject> = emptyList(),
    val filteredSubjects: List<BesteSchuleSubject> = emptyList(),
)

sealed class AnalyticsAction {
    data class ToggleSubjectFilter(val subject: BesteSchuleSubject) : AnalyticsAction()

    data class SetInterval(val interval: BesteSchuleInterval) : AnalyticsAction()
}
