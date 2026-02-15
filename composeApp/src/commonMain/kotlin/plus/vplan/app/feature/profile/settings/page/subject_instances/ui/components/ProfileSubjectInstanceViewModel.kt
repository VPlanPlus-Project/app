package plus.vplan.app.feature.profile.settings.page.subject_instances.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import plus.vplan.app.domain.model.Course
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.usecase.GetProfileByIdUseCase
import plus.vplan.app.feature.profile.domain.usecase.UpdateIndicesUseCase
import plus.vplan.app.feature.profile.settings.page.subject_instances.domain.usecase.GetCourseConfigurationUseCase
import plus.vplan.app.feature.profile.settings.page.subject_instances.domain.usecase.SetProfileSubjectInstanceEnabledUseCase
import plus.vplan.app.utils.filterKeysNotNull
import kotlin.uuid.Uuid

class ProfileSubjectInstanceViewModel(
    private val getProfileByIdUseCase: GetProfileByIdUseCase,
    private val getCourseConfigurationUseCase: GetCourseConfigurationUseCase,
    private val setProfileSubjectInstanceEnabledUseCase: SetProfileSubjectInstanceEnabledUseCase,
    private val updateIndicesUseCase: UpdateIndicesUseCase,
    private val subjectInstanceRepository: SubjectInstanceRepository,
) : ViewModel() {
    var state by mutableStateOf(ProfileSubjectInstanceState())
        private set

    private var shouldRebuildIndicesOnProfileReload: Boolean = false

    fun init(profileId: Uuid) {
        state = ProfileSubjectInstanceState()
        viewModelScope.launch {
            getProfileByIdUseCase(profileId).collectLatest { profile ->
                if (profile !is Profile.StudentProfile) return@collectLatest
                if (shouldRebuildIndicesOnProfileReload) {
                    shouldRebuildIndicesOnProfileReload = false
                    updateIndicesUseCase(profile)
                }
                state = state.copy(
                    profile = profile,
                    courses = getCourseConfigurationUseCase(profile),
                    subjectInstance = profile.subjectInstanceConfiguration
                        .mapKeys { subjectInstanceRepository.getByLocalId(it.key).first() }
                        .filterKeysNotNull()
                        .toList()
                        .sortedBy { runBlocking { it.first.subject + (it.first.getCourseItem()?.name ?: "") + (it.first.getTeacherItem()?.name ?: "") } }
                        .toMap()
                )
            }
        }
    }

    fun onEvent(event: ProfileSubjectInstanceEvent) {
        viewModelScope.launch {
            when (event) {
                is ProfileSubjectInstanceEvent.ToggleCourseSelection -> {
                    state.profile!!.subjectInstanceConfiguration
                        .keys
                        .mapNotNull { subjectInstanceRepository.getByLocalId(it).first() }
                        .filter { it.getCourseItem()?.id == event.course.id }
                        .let { subjectInstances ->
                            setProfileSubjectInstanceEnabledUseCase(state.profile!!, subjectInstances, event.isSelected)
                            shouldRebuildIndicesOnProfileReload = true
                            state = state.copy(subjectInstance = state.subjectInstance.plus(subjectInstances.map { it to event.isSelected }))
                        }
                }
                is ProfileSubjectInstanceEvent.ToggleSubjectInstanceSelection -> {
                    setProfileSubjectInstanceEnabledUseCase(state.profile!!, event.subjectInstance, event.isSelected)
                    shouldRebuildIndicesOnProfileReload = true
                    state = state.copy(subjectInstance = state.subjectInstance.plus(event.subjectInstance to event.isSelected))
                }
            }
        }
    }
}

data class ProfileSubjectInstanceState(
    val profile: Profile.StudentProfile? = null,
    val courses: Map<Course, Boolean?> = emptyMap(),
    val subjectInstance: Map<SubjectInstance, Boolean> = emptyMap()
)

sealed class ProfileSubjectInstanceEvent {
    data class ToggleCourseSelection(val course: Course, val isSelected: Boolean) : ProfileSubjectInstanceEvent()
    data class ToggleSubjectInstanceSelection(val subjectInstance: SubjectInstance, val isSelected: Boolean) : ProfileSubjectInstanceEvent()
}