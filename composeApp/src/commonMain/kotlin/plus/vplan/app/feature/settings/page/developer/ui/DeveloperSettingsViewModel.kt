package plus.vplan.app.feature.settings.page.developer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import plus.vplan.app.data.source.database.model.database.DbFcmLog
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.FcmRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.sync.domain.usecase.fullsync.FullSyncCause
import plus.vplan.app.feature.sync.domain.usecase.fullsync.FullSyncUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateTimetableUseCase
import plus.vplan.app.utils.now

class DeveloperSettingsViewModel(
    private val fullSyncUseCase: FullSyncUseCase,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val timetableRepository: TimetableRepository,
    private val keyValueRepository: KeyValueRepository,
    private val fcmRepository: FcmRepository,
    private val updateSubstitutionPlanUseCase: UpdateSubstitutionPlanUseCase,
    private val updateTimetableUseCase: UpdateTimetableUseCase,
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
) : ViewModel() {

    var state by mutableStateOf(DeveloperSettingsState())

    init {
        viewModelScope.launch {
            getCurrentProfileUseCase().collect { profile ->
                state = state.copy(
                    profile = profile,
                )
            }
        }
        viewModelScope.launch {
            keyValueRepository.get(Keys.DEVELOPER_SETTINGS_DISABLE_AUTO_SYNC).collectLatest {
                state = state.copy(
                    isAutoSyncDisabled = it.toBoolean()
                )
            }
        }
        viewModelScope.launch {
            fcmRepository.getAll().collectLatest {
                state = state.copy(
                    fcmLogs = it.sortedByDescending { log -> log.timestamp }.take(100)
                )
            }
        }
    }

    fun handleEvent(event: DeveloperSettingsEvent) {
        viewModelScope.launch {
            when (event) {
                DeveloperSettingsEvent.StartFullSync -> {
                    if (state.isFullSyncRunning) return@launch
                    state = state.copy(isFullSyncRunning = true)
                    launch { fullSyncUseCase(FullSyncCause.Manual).join() }.invokeOnCompletion {
                        state = state.copy(isFullSyncRunning = false)
                    }
                }
                DeveloperSettingsEvent.ClearLessonCache -> {
                    substitutionPlanRepository.deleteAllSubstitutionPlans()
                    timetableRepository.deleteAllTimetables()
                }
                DeveloperSettingsEvent.DeleteSubstitutionPlan -> {
                    substitutionPlanRepository.deleteAllSubstitutionPlans()
                }
                DeveloperSettingsEvent.UpdateSubstitutionPlan -> {
                    if (state.isSubstitutionPlanUpdateRunning) return@launch
                    state = state.copy(isSubstitutionPlanUpdateRunning = true)
                    updateSubstitutionPlanUseCase(
                        state.profile!!.getSchool().getFirstValue()!!, listOf(LocalDate.now(), LocalDate.now().plus(1, DateTimeUnit.DAY)),
                        allowNotification = true
                    )
                    state = state.copy(isSubstitutionPlanUpdateRunning = false)
                }
                DeveloperSettingsEvent.UpdateTimetable -> {
                    if (state.isTimetableUpdateRunning) return@launch
                    state = state.copy(isTimetableUpdateRunning = true)
                    updateTimetableUseCase(state.profile!!.getSchool().getFirstValue()!!, forceUpdate = true)
                    state = state.copy(isTimetableUpdateRunning = false)
                }
                DeveloperSettingsEvent.ToggleAutoSyncDisabled -> {
                    keyValueRepository.set(Keys.DEVELOPER_SETTINGS_DISABLE_AUTO_SYNC, (!keyValueRepository.get(Keys.DEVELOPER_SETTINGS_DISABLE_AUTO_SYNC).first().toBoolean()).toString())
                }
            }
        }
    }
}

data class DeveloperSettingsState(
    val isFullSyncRunning: Boolean = false,
    val isSubstitutionPlanUpdateRunning: Boolean = false,
    val isTimetableUpdateRunning: Boolean = false,
    val profile: Profile? = null,
    val isAutoSyncDisabled: Boolean = false,
    val fcmLogs: List<DbFcmLog> = emptyList()
)

sealed class DeveloperSettingsEvent {
    object StartFullSync : DeveloperSettingsEvent()
    object ClearLessonCache : DeveloperSettingsEvent()
    object UpdateSubstitutionPlan : DeveloperSettingsEvent()
    object UpdateTimetable : DeveloperSettingsEvent()
    data object ToggleAutoSyncDisabled : DeveloperSettingsEvent()
    data object DeleteSubstitutionPlan : DeveloperSettingsEvent()
}