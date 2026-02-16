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
import org.koin.core.component.inject
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Course
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.populated.CoursePopulator
import plus.vplan.app.domain.model.populated.PopulatedCourse
import plus.vplan.app.domain.model.populated.PopulatedSubjectInstance
import plus.vplan.app.domain.model.populated.SubjectInstancePopulator
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
) : ViewModel(), KoinComponent {
    private val subjectInstancePopulator by inject<SubjectInstancePopulator>()
    private val coursePopulator by inject<CoursePopulator>()

    val state: StateFlow<ProfileSubjectInstanceState>
        field = MutableStateFlow(ProfileSubjectInstanceState())

    private var shouldRebuildIndicesOnProfileReload: Boolean = false

    fun init(profileId: Uuid) {
        state.update { ProfileSubjectInstanceState() }
        viewModelScope.launch {
            getProfileByIdUseCase(profileId).collectLatest { profile ->
                if (profile !is Profile.StudentProfile) return@collectLatest
                if (shouldRebuildIndicesOnProfileReload) {
                    shouldRebuildIndicesOnProfileReload = false
                    updateIndicesUseCase(profile)
                }

                state.update { state ->
                    state.copy(
                        profile = profile,
                        courses = getCourseConfigurationUseCase(profile)
                            .mapKeys { coursePopulator.populateSingle(it.key).first() },
                        subjectInstances = profile.subjectInstanceConfiguration
                            .mapKeys { subjectInstancePopulator.populateSingle(subjectInstanceRepository.getByLocalId(it.key).first() ?: return@mapKeys null).first() }
                            .filterKeysNotNull()
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
                        .mapNotNull { subjectInstanceRepository.getByLocalId(it).first() }
                        .filter { it.courseId == event.course.id }
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
    val courses: Map<PopulatedCourse, Boolean?> = emptyMap(),
    val subjectInstances: Map<PopulatedSubjectInstance, Boolean> = emptyMap(),
)

sealed class ProfileSubjectInstanceEvent {
    data class ToggleCourseSelection(val course: Course, val isSelected: Boolean) : ProfileSubjectInstanceEvent()
    data class ToggleSubjectInstanceSelection(val subjectInstance: SubjectInstance, val isSelected: Boolean) : ProfileSubjectInstanceEvent()
}