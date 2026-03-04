package plus.vplan.app.feature.profile.settings.page.subject_instances.ui.components

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepository
import plus.vplan.app.core.model.Course
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.feature.profile.domain.usecase.UpdateIndicesUseCase
import plus.vplan.app.feature.profile.settings.page.subject_instances.domain.usecase.GetCourseConfigurationUseCase
import plus.vplan.app.feature.profile.settings.page.subject_instances.domain.usecase.SetProfileSubjectInstanceEnabledUseCase
import plus.vplan.app.utils.sortedBySuspending
import kotlin.uuid.Uuid

class ProfileSubjectInstanceViewModel(
    private val profileRepository: ProfileRepository,
    private val getCourseConfigurationUseCase: GetCourseConfigurationUseCase,
    private val setProfileSubjectInstanceEnabledUseCase: SetProfileSubjectInstanceEnabledUseCase,
    private val updateIndicesUseCase: UpdateIndicesUseCase,
    private val subjectInstanceRepository: SubjectInstanceRepository,
) : ViewModel(), KoinComponent {
    val state: StateFlow<ProfileSubjectInstanceState>
        field = MutableStateFlow(ProfileSubjectInstanceState())

    private var shouldRebuildIndicesOnProfileReload: Boolean = false

    fun init(profileId: Uuid) {
        state.update { ProfileSubjectInstanceState() }
        viewModelScope.launch {
            profileRepository.getById(profileId).collectLatest { profile ->
                if (profile !is Profile.StudentProfile) return@collectLatest
                if (shouldRebuildIndicesOnProfileReload) {
                    shouldRebuildIndicesOnProfileReload = false
                    updateIndicesUseCase(profile)
                }

                state.update { state ->
                    state.copy(
                        profile = profile,
                        courses = getCourseConfigurationUseCase(profile),
                        subjectInstances = subjectInstanceRepository.getByGroup(profile.group).first().associate { it to true } +
                                profile.subjectInstanceConfiguration
                                    .sortedBySuspending { it.key.subject }
                                    .associate { it.key to it.value }
                    )
                }
            }
        }
    }

    fun onEvent(event: ProfileSubjectInstanceEvent) {
        viewModelScope.launch {
            when (event) {
                is ProfileSubjectInstanceEvent.ToggleCourseSelection -> {
                    state.value.profile!!.subjectInstanceConfiguration
                        .keys
                        .filter { it.course?.id == event.course.id }
                        .let { subjectInstances ->
                            setProfileSubjectInstanceEnabledUseCase(state.value.profile!!, subjectInstances, event.isSelected)
                            shouldRebuildIndicesOnProfileReload = true
                            //state = state.copy(subjectInstances = state.subjectInstances.plus(subjectInstances.map { it to event.isSelected }))
                        }
                }
                is ProfileSubjectInstanceEvent.ToggleSubjectInstanceSelection -> {
                    setProfileSubjectInstanceEnabledUseCase(state.value.profile!!, event.subjectInstance, event.isSelected)
                    shouldRebuildIndicesOnProfileReload = true
                    //state = state.copy(subjectInstances = state.subjectInstances.plus(event.subjectInstance to event.isSelected))
                }
            }
        }
    }
}

@Immutable
data class ProfileSubjectInstanceState(
    val profile: Profile.StudentProfile? = null,
    val courses: List<Pair<Course, Boolean?>> = emptyList(),
    val subjectInstances: Map<SubjectInstance, Boolean> = emptyMap(),
)

sealed class ProfileSubjectInstanceEvent {
    data class ToggleCourseSelection(val course: Course, val isSelected: Boolean) : ProfileSubjectInstanceEvent()
    data class ToggleSubjectInstanceSelection(val subjectInstance: SubjectInstance, val isSelected: Boolean) : ProfileSubjectInstanceEvent()
}