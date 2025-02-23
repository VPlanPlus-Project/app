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
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.schulverwalter.Grade
import plus.vplan.app.domain.model.schulverwalter.Interval
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
                    .map { it
                        .filterIsInstance<CacheState.Done<Grade>>()
                        .map { gradeState -> gradeState.data }
                        .filter { grade -> grade.vppIdId == vppIdId }
                    }.collectLatest {
                        state = state.copy(grades = it)
                    }
            }
        }
    }
}

data class AnalyticsState(
    val vppId: VppId? = null,
    val interval: Interval? = null,
    val grades: List<Grade> = emptyList()
)