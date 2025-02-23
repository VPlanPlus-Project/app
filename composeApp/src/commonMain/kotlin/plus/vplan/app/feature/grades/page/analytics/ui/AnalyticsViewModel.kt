package plus.vplan.app.feature.grades.page.analytics.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.schulverwalter.Grade
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.domain.model.schulverwalter.Subject
import plus.vplan.app.feature.grades.domain.usecase.GetCurrentIntervalUseCase

class AnalyticsViewModel(
    private val getCurrentIntervalUseCase: GetCurrentIntervalUseCase
) : ViewModel() {
    var state by mutableStateOf(AnalyticsState())
        private set

    fun init(vppIdId: Int) {
        state = AnalyticsState()
        viewModelScope.launch {
            state = state.copy(interval = getCurrentIntervalUseCase())
            App.vppIdSource.getById(vppIdId).filterIsInstance<CacheState.Done<VppId.Active>>().map { it.data }.collectLatest { vppId ->
                state = state.copy(vppId = vppId)
                App.gradeSource.getAll()
                    .map {
                        it
                            .filterIsInstance<CacheState.Done<Grade>>()
                            .map { gradeState -> gradeState.data }
                            .filter { grade -> grade.vppIdId == vppIdId }
                    }.collectLatest { grades ->
                        state = state.copy(
                            grades = grades,
                            filteredGrades = grades,
                            availableSubjectFilters = grades.map { it.subject.getFirstValue()!! }.distinctBy { subject -> subject.id }.sortedBy { it.localId },
                            filteredSubjects = emptyList()
                        )
                    }
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
            }
        }
    }

    private suspend fun updateFiltered() {
        state = state.copy(filteredGrades = state.grades
            .filter { grade ->
                state.filteredSubjects.any { grade.subject.getFirstValue()!!.id == it.id } || state.filteredSubjects.isEmpty()
            })
    }
}

data class AnalyticsState(
    val vppId: VppId? = null,
    val interval: Interval? = null,
    val grades: List<Grade> = emptyList(),
    val filteredGrades: List<Grade> = emptyList(),

    val availableSubjectFilters: List<Subject> = emptyList(),
    val filteredSubjects: List<Subject> = emptyList()
)

sealed class AnalyticsAction {
    data class ToggleSubjectFilter(val subject: Subject) : AnalyticsAction()
}