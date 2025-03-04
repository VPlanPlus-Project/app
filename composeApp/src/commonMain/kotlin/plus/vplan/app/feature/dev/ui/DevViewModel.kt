package plus.vplan.app.feature.dev.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.feature.sync.domain.usecase.schulverwalter.SyncGradesUseCase
import kotlin.uuid.Uuid

class DevViewModel(
    private val keyValueRepository: KeyValueRepository,
    private val assessmentRepository: AssessmentRepository,
    private val homeworkRepository: HomeworkRepository,
    private val platformNotificationRepository: PlatformNotificationRepository,
    private val syncGradesUseCase: SyncGradesUseCase,
) : ViewModel() {
    var state by mutableStateOf(DevState())
        private set

    init {
        viewModelScope.launch {
            keyValueRepository.get(Keys.CURRENT_PROFILE).filterNotNull().collectLatest { profileId ->
                App.profileSource.getById(Uuid.parseHex(profileId))
                    .filterIsInstance<CacheState.Done<Profile>>()
                    .collectLatest { state = state.copy(profile = it.data) }
            }
        }

        viewModelScope.launch {
            App.assessmentSource.getAll().map { assessments -> assessments.filterIsInstance<CacheState.Done<Assessment>>().map { it.data } }.collect {
                state = state.copy(assessments = it.onEach { assessment ->
                    assessment.prefetch()
                })
            }
        }
    }

    fun onEvent(event: DevEvent) {
        viewModelScope.launch {
            when (event) {
                DevEvent.Refresh -> {
                    Logger.d { "Assessment update started" }
                    assessmentRepository.download(
                        schoolApiAccess = (state.profile as Profile.StudentProfile).let {
                            it.getVppIdItem()?.buildSchoolApiAccess() ?: it.getSchoolItem().getSchoolApiAccess()
                        } ?: return@launch,
                        defaultLessonIds = (state.profile as Profile.StudentProfile).getDefaultLessons().map { it.id })
                    Logger.d { "Assessments updated" }

                    Logger.d { "Homework update started" }
                    homeworkRepository.download(
                        schoolApiAccess = (state.profile as Profile.StudentProfile).let {
                            it.getVppIdItem()?.buildSchoolApiAccess() ?: it.getSchoolItem().getSchoolApiAccess()
                        } ?: return@launch,
                        groupId = (state.profile as Profile.StudentProfile).group,
                        defaultLessonIds = (state.profile as Profile.StudentProfile).getDefaultLessons().map { it.id }
                    )
                    Logger.d { "Homework updated" }
                }

                DevEvent.Clear -> assessmentRepository.clearCache()
                DevEvent.Sync -> syncGradesUseCase(true)
                DevEvent.Notify -> platformNotificationRepository.sendNotification("Test", "Test", "Profil")
            }
        }
    }
}

data class DevState(
    val profile: Profile? = null,
    val assessments: List<Assessment> = emptyList(),
    val updateResponse: Response.Error? = null
)

sealed class DevEvent {
    data object Refresh : DevEvent()
    data object Clear : DevEvent()

    data object Sync : DevEvent()
    data object Notify : DevEvent()
}

private suspend fun Assessment.prefetch() {
    this.getSubjectInstanceItem()
    when (this.creator) {
        is AppEntity.VppId -> this.getCreatedByVppIdItem()
        is AppEntity.Profile -> this.getCreatedByProfileItem()!!.getGroupItem()
    }
}