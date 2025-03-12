package plus.vplan.app.feature.profile.settings.page.subject_instances.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.usecase.GetProfileByIdUseCase
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileLessonIndexUseCase
import plus.vplan.app.feature.profile.settings.page.subject_instances.domain.usecase.GetCourseConfigurationUseCase
import plus.vplan.app.feature.profile.settings.page.subject_instances.domain.usecase.SetProfileSubjectInstanceEnabledUseCase
import kotlin.uuid.Uuid

class ProfileSubjectInstanceViewModel(
    private val getProfileByIdUseCase: GetProfileByIdUseCase,
    private val getCourseConfigurationUseCase: GetCourseConfigurationUseCase,
    private val setProfileSubjectInstanceEnabledUseCase: SetProfileSubjectInstanceEnabledUseCase,
    private val updateProfileLessonIndexUseCase: UpdateProfileLessonIndexUseCase
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
                    updateProfileLessonIndexUseCase(profile)
                }
                profile.getSubjectInstances().onEach {
                    it.getCourseItem()
                    it.getTeacherItem()
                }
                state = state.copy(
                    profile = profile,
                    courses = getCourseConfigurationUseCase(profile),
                    subjectInstance = profile.subjectInstanceConfiguration
                        .mapKeys { (key, _) -> profile.subjectInstanceItems.first { it.id == key } }
                        .toList()
                        .sortedBy { runBlocking { it.first.subject + (it.first.getCourseItem()?.name ?: "") + (it.first.getTeacherItem()?.name ?: "") } }
                        .toMap()
                )

                Logger.d { state.subjectInstance.keys.first().getTeacherItem().let {
                    it?.name + " " + it.hashCode()
                } }
            }
        }
    }

    fun onEvent(event: ProfileSubjectInstanceEvent) {
        viewModelScope.launch {
            when (event) {
                is ProfileSubjectInstanceEvent.ToggleCourseSelection -> {
                    state.profile!!.getSubjectInstances()
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