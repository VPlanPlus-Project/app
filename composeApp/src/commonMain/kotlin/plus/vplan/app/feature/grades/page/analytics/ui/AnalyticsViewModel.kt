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
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.schulverwalter.Grade
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.domain.model.schulverwalter.Subject
import plus.vplan.app.feature.grades.domain.usecase.GetCurrentIntervalUseCase
import plus.vplan.app.feature.grades.domain.usecase.GetIntervalsUseCase

class AnalyticsViewModel(
    private val getCurrentIntervalUseCase: GetCurrentIntervalUseCase,
    private val getIntervalsUseCase: GetIntervalsUseCase
) : ViewModel() {
    var state by mutableStateOf(AnalyticsState())
        private set

    fun init(vppIdId: Int) {
        state = AnalyticsState()
        viewModelScope.launch { getIntervalsUseCase().collectLatest { state = state.copy(intervals = it) } }
        viewModelScope.launch {
            getCurrentIntervalUseCase().let { state = state.copy(interval = it) }
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
                            filteredGrades = emptyList(),
                            availableSubjectFilters = grades.map { it.subject.getFirstValueOld()!! }.distinctBy { subject -> subject.id }.sortedBy { it.localId },
                            filteredSubjects = emptyList()
                        )
                        updateFiltered()
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
                is AnalyticsAction.SetTimeType -> {
                    state = state.copy(timeType = event.timeType)
                    updateTimeDataPoints()
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
                state.filteredSubjects.any { grade.subject.getFirstValueOld()!!.id == it.id } || state.filteredSubjects.isEmpty()
            }
            .filter { it.collection.getFirstValueOld()!!.intervalId in listOfNotNull(state.interval?.id, state.interval?.includedIntervalId) }
        )
        updateTimeDataPoints()
    }

    private suspend fun updateTimeDataPoints() {
        val grades = state.filteredGrades
        val dataPoints = mutableListOf<AnalyticsTimeDataPoint>()
        when (state.timeType) {
            AnalyticsTimeType.Average -> Unit
            AnalyticsTimeType.Value -> grades.forEach { grade ->
                val value = grade.numericValue ?: return@forEach
                dataPoints.add(AnalyticsTimeDataPoint(grade.givenAt, grade.subject.getFirstValueOld()!!.id, value.toDouble()))
            }
        }
        state = state.copy(timeDataPoints = dataPoints.sortedBy { it.date })
    }
}

data class AnalyticsState(
    val vppId: VppId? = null,
    val interval: Interval? = null,
    val intervals: List<Interval> = emptyList(),
    val grades: List<Grade> = emptyList(),
    val filteredGrades: List<Grade> = emptyList(),

    val availableSubjectFilters: List<Subject> = emptyList(),
    val filteredSubjects: List<Subject> = emptyList(),

    val timeDataPoints: List<AnalyticsTimeDataPoint> = emptyList(),

    val timeType: AnalyticsTimeType = AnalyticsTimeType.Average
)

sealed class AnalyticsAction {
    data class ToggleSubjectFilter(val subject: Subject) : AnalyticsAction()
    data class SetTimeType(val timeType: AnalyticsTimeType) : AnalyticsAction()

    data class SetInterval(val interval: Interval) : AnalyticsAction()
}

enum class AnalyticsTimeType {
    Average, Value
}

data class AnalyticsTimeDataPoint(
    val date: LocalDate,
    val subjectId: Int,
    val value: Double
)