package plus.vplan.app.feature.home.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.DefaultLessonRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateDefaultLessonsUseCase

class HomeViewModel(
    private val schoolRepository: SchoolRepository,
    private val groupRepository: GroupRepository,
    private val defaultLessonRepository: DefaultLessonRepository,
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
            defaultLessonRepository.getByGroup(1721).collect { defaultLessons ->
                state = state.copy(defaultLessons = defaultLessons.sortedBy { it.subject })
            }
        }
    }

    fun onEvent(event: HomeUiEvent) {
        viewModelScope.launch {
            when (event) {
                HomeUiEvent.Update -> updateDefaultLessonsUseCase(state.school as School.IndiwareSchool)
                is HomeUiEvent.Delete -> {
                    if (event.all) defaultLessonRepository.deleteById(state.defaultLessons.map { it.id })
                    else defaultLessonRepository.deleteById(state.defaultLessons.filter { it.subject == "CH" }.map { it.id })
                }
                HomeUiEvent.SneakWeekIn -> {
                    defaultLessonRepository.upsert(DefaultLesson(
                        "idk",
                        "Sneak In",
                        state.group!!,
                        null,
                        null
                    ))
                }
            }
        }
    }
}

data class HomeUiState(
    val school: School? = null,
    val group: Group? = null,
    val defaultLessons: List<DefaultLesson> = emptyList()
)

sealed class HomeUiEvent {
    data object Update : HomeUiEvent()
    data class Delete(val all: Boolean) : HomeUiEvent()
    data object SneakWeekIn : HomeUiEvent()
}