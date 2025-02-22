package plus.vplan.app.feature.profile.settings.page.subject_instances.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetProfileByIdUseCase
import plus.vplan.app.feature.profile.settings.page.subject_instances.domain.usecase.GetCourseConfigurationUseCase
import plus.vplan.app.feature.profile.settings.page.subject_instances.domain.usecase.SetProfileDefaultLessonEnabledUseCase
import kotlin.uuid.Uuid

class ProfileSubjectInstanceViewModel(
    private val getProfileByIdUseCase: GetProfileByIdUseCase,
    private val getCourseConfigurationUseCase: GetCourseConfigurationUseCase,
    private val setProfileDefaultLessonEnabledUseCase: SetProfileDefaultLessonEnabledUseCase
) : ViewModel() {
    var state by mutableStateOf(ProfileSubjectInstanceState())
        private set

    fun init(profileId: Uuid) {
        state = ProfileSubjectInstanceState()
        viewModelScope.launch {
            getProfileByIdUseCase(profileId).collectLatest { profile ->
                if (profile !is Profile.StudentProfile) return@collectLatest
                profile.getDefaultLessons().onEach {
                    it.getCourseItem()
                    it.getTeacherItem()
                }
                state = state.copy(
                    profile = profile,
                    courses = getCourseConfigurationUseCase(profile),
                    defaultLessons = profile.defaultLessons
                        .mapKeys { (key, _) -> profile.defaultLessonItems.first { it.id == key } }
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
                    state.profile!!.getDefaultLessons()
                        .filter { it.getCourseItem()?.id == event.course.id }
                        .forEach { defaultLesson ->
                            setProfileDefaultLessonEnabledUseCase(state.profile!!, defaultLesson, event.isSelected)
                            state = state.copy(defaultLessons = state.defaultLessons.plus(defaultLesson to event.isSelected))
                        }
                }
                is ProfileSubjectInstanceEvent.ToggleDefaultLessonSelection -> {
                    setProfileDefaultLessonEnabledUseCase(state.profile!!, event.defaultLesson, event.isSelected)
                    state = state.copy(defaultLessons = state.defaultLessons.plus(event.defaultLesson to event.isSelected))
                }
            }
        }
    }
}

data class ProfileSubjectInstanceState(
    val profile: Profile.StudentProfile? = null,
    val courses: Map<Course, Boolean?> = emptyMap(),
    val defaultLessons: Map<DefaultLesson, Boolean> = emptyMap()
)

sealed class ProfileSubjectInstanceEvent {
    data class ToggleCourseSelection(val course: Course, val isSelected: Boolean) : ProfileSubjectInstanceEvent()
    data class ToggleDefaultLessonSelection(val defaultLesson: DefaultLesson, val isSelected: Boolean) : ProfileSubjectInstanceEvent()
}