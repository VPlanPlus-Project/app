package plus.vplan.app.feature.settings.page.developer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.data.source.database.model.database.DbFcmLog
import plus.vplan.app.core.model.getFirstValue
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.domain.repository.FcmRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.sync.domain.usecase.besteschule.SyncGradesUseCase
import plus.vplan.app.feature.sync.domain.usecase.fullsync.FullSyncCause
import plus.vplan.app.feature.sync.domain.usecase.fullsync.FullSyncUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateLessonTimesUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateSubjectInstanceUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateTimetableUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateWeeksUseCase
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateAssessmentsUseCase
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateHomeworkUseCase
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
    private val updateWeeksUseCase: UpdateWeeksUseCase,
    private val updateLessonTimesUseCase: UpdateLessonTimesUseCase,
    private val updateSubjectInstanceUseCase: UpdateSubjectInstanceUseCase,
    private val updateHomeworkUseCase: UpdateHomeworkUseCase,
    private val updateAssessmentsUseCase: UpdateAssessmentsUseCase,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val groupRepository: GroupRepository
) : ViewModel(), KoinComponent {

    var state by mutableStateOf(DeveloperSettingsState())

    private val syncGradesUseCase by inject<SyncGradesUseCase>()

    init {
        viewModelScope.launch {
            keyValueRepository.get(Keys.DEVELOPER_SETTINGS_ACTIVE).collectLatest {
                state = state.copy(
                    isDeveloperModeEnabled = it.toBoolean()
                )
            }
        }
        viewModelScope.launch {
            getCurrentProfileUseCase().collectLatest { profile ->
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
                DeveloperSettingsEvent.ClearSubstitutionPlanCache -> {
                    substitutionPlanRepository.deleteAllSubstitutionPlans()
                }
                DeveloperSettingsEvent.UpdateSubstitutionPlan -> {
                    if (state.isSubstitutionPlanUpdateRunning) return@launch
                    state = state.copy(isSubstitutionPlanUpdateRunning = true)
                    updateSubstitutionPlanUseCase(
                        state.profile!!.school, listOf(LocalDate.now(), LocalDate.now().plus(1, DateTimeUnit.DAY)),
                        allowNotification = true
                    )
                    state = state.copy(isSubstitutionPlanUpdateRunning = false)
                }
                DeveloperSettingsEvent.UpdateTimetable -> {
                    if (state.isTimetableUpdateRunning) return@launch
                    state = state.copy(isTimetableUpdateRunning = true)
                    updateTimetableUseCase(state.profile!!.school, forceUpdate = true)
                    state = state.copy(isTimetableUpdateRunning = false)
                }
                DeveloperSettingsEvent.UpdateWeeks -> {
                    if (state.isWeekUpdateRunning) return@launch
                    state = state.copy(isWeekUpdateRunning = true)
                    updateWeeksUseCase(state.profile!!.school)
                    state = state.copy(isWeekUpdateRunning = false)
                }
                DeveloperSettingsEvent.UpdateSubjectInstances -> {
                    if (state.isSubjectInstanceUpdateRunning) return@launch
                    state = state.copy(isSubjectInstanceUpdateRunning = true)
                    updateSubjectInstanceUseCase(
                        school = state.profile!!.school,
                        providedClient = null
                    )
                    state = state.copy(isSubjectInstanceUpdateRunning = false)
                }
                DeveloperSettingsEvent.UpdateLessonTimes -> {
                    if (state.isLessonTimesUpdateRunning) return@launch
                    state = state.copy(isLessonTimesUpdateRunning = true)
                    updateLessonTimesUseCase(
                        state.profile!!.school,
                        null
                    )
                    state = state.copy(isLessonTimesUpdateRunning = false)
                }
                DeveloperSettingsEvent.UpdateGrades -> {
                    if (state.isGradeUpdateRunning) return@launch
                    state = state.copy(isGradeUpdateRunning = true)
                    try {
                        syncGradesUseCase(false)
                    } catch (e: Exception) {
                        Logger.e { "Failed to sync grades: $e" }
                    } finally {
                        state = state.copy(isGradeUpdateRunning = false)
                    }
                }
                DeveloperSettingsEvent.ToggleAutoSyncDisabled -> {
                    keyValueRepository.set(Keys.DEVELOPER_SETTINGS_DISABLE_AUTO_SYNC, (!keyValueRepository.get(Keys.DEVELOPER_SETTINGS_DISABLE_AUTO_SYNC).first().toBoolean()).toString())
                }
                is DeveloperSettingsEvent.UpdateSubjectInstance -> {
                    subjectInstanceRepository.findByAliases(event.subjectInstance.aliases, forceUpdate = true, preferCurrentState = false).getFirstValue().also {
                        Logger.i { "Updated subject instance.\nWas: ${event.subjectInstance}\nIs:  $it" }
                    }
                }
                is DeveloperSettingsEvent.UpdateGroup -> {
                    groupRepository.findByAliases(event.group.aliases, forceUpdate = true, preferCurrentState = false).getFirstValue().also {
                        Logger.i { "Updated group.\nWas: ${event.group}\nIs:  $it" }
                    }
                }
                DeveloperSettingsEvent.UpdateHomework -> {
                    if (state.profile == null) return@launch
                    if (state.isHomeworkUpdateRunning) return@launch
                    state = state.copy(isHomeworkUpdateRunning = true)
                    updateHomeworkUseCase(true)
                    state = state.copy(isHomeworkUpdateRunning = false)
                }
                DeveloperSettingsEvent.UpdateAssessments -> {
                    if (state.profile == null) return@launch
                    if (state.isAssessmentsUpdateRunning) return@launch
                    state = state.copy(isAssessmentsUpdateRunning = true)
                    updateAssessmentsUseCase(true)
                    state = state.copy(isAssessmentsUpdateRunning = false)
                }
                DeveloperSettingsEvent.ToggleDeveloperMode -> {
                    keyValueRepository.set(Keys.DEVELOPER_SETTINGS_ACTIVE, (!state.isDeveloperModeEnabled).toString())
                }
            }
        }
    }
}

data class DeveloperSettingsState(
    val isFullSyncRunning: Boolean = false,
    val isWeekUpdateRunning: Boolean = false,
    val isSubjectInstanceUpdateRunning: Boolean = false,
    val isSubstitutionPlanUpdateRunning: Boolean = false,
    val isTimetableUpdateRunning: Boolean = false,
    val isLessonTimesUpdateRunning: Boolean = false,
    val isHomeworkUpdateRunning: Boolean = false,
    val isAssessmentsUpdateRunning: Boolean = false,
    val isGradeUpdateRunning: Boolean = false,
    val isDeveloperModeEnabled: Boolean = true,
    val profile: Profile? = null,
    val isAutoSyncDisabled: Boolean = false,
    val fcmLogs: List<DbFcmLog> = emptyList()
)

sealed class DeveloperSettingsEvent {
    data object StartFullSync : DeveloperSettingsEvent()
    data object ClearLessonCache : DeveloperSettingsEvent()
    data object UpdateSubstitutionPlan : DeveloperSettingsEvent()
    data object UpdateTimetable : DeveloperSettingsEvent()
    data object UpdateWeeks : DeveloperSettingsEvent()
    data object UpdateLessonTimes : DeveloperSettingsEvent()
    data object UpdateSubjectInstances : DeveloperSettingsEvent()
    data class UpdateGroup(val group: Group) : DeveloperSettingsEvent()
    data class UpdateSubjectInstance(val subjectInstance: SubjectInstance) : DeveloperSettingsEvent()
    data object UpdateHomework : DeveloperSettingsEvent()
    data object UpdateAssessments : DeveloperSettingsEvent()
    data object UpdateGrades: DeveloperSettingsEvent()
    data object ToggleDeveloperMode : DeveloperSettingsEvent()
    data object ToggleAutoSyncDisabled : DeveloperSettingsEvent()
    data object ClearSubstitutionPlanCache : DeveloperSettingsEvent()
}