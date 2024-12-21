package plus.vplan.app.feature.home.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.CourseRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateDefaultLessonsUseCase

class HomeViewModel(
    private val schoolRepository: SchoolRepository,
    private val groupRepository: GroupRepository,
    private val courseRepository: CourseRepository,
    private val updateDefaultLessonsUseCase: UpdateDefaultLessonsUseCase
) : ViewModel() {
    var state by mutableStateOf(HomeUiState())
        private set

    init {
        viewModelScope.launch {
            state = state.copy(
                school = schoolRepository.getById(67).first(),
                group = groupRepository.getById(1721).first()
            )
            courseRepository.getByGroup(1721).collect { courses ->
                state = state.copy(courses = courses.sortedBy { it.id })
            }
        }
    }

    fun onEvent(event: HomeUiEvent) {
        viewModelScope.launch {
            when (event) {
                HomeUiEvent.Update -> updateDefaultLessonsUseCase(state.school as School.IndiwareSchool)
                is HomeUiEvent.Delete -> {
                    if (event.all) courseRepository.deleteById(state.courses.map { it.id })
                    else courseRepository.deleteById(state.courses.filter { it.name.uppercase() == it.name }.map { it.id })
                }
                HomeUiEvent.SneakWeekIn -> {
                    courseRepository.upsert(Course.fromIndiware(
                        sp24SchoolId = (state.school as School.IndiwareSchool).sp24Id,
                        group = state.group!!,
                        name = "Sneak In",
                        teacher = null
                    ))
                }
            }
        }
    }
}

data class HomeUiState(
    val school: School? = null,
    val group: Group? = null,
    val courses: List<Course> = emptyList()
)

sealed class HomeUiEvent {
    data object Update : HomeUiEvent()
    data class Delete(val all: Boolean) : HomeUiEvent()
    data object SneakWeekIn : HomeUiEvent()
}