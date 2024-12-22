package plus.vplan.app.feature.home.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateTimetableUseCase

class HomeViewModel(
    private val schoolRepository: SchoolRepository,
    private val groupRepository: GroupRepository,
    private val timetableRepository: TimetableRepository,
    private val keyValueRepository: KeyValueRepository,
    private val updateTimetableUseCase: UpdateTimetableUseCase
) : ViewModel() {
    var state by mutableStateOf(HomeUiState())
        private set

    init {
        viewModelScope.launch {
            state = state.copy(
                school = schoolRepository.getById(67).first(),
                group = groupRepository.getById(1721).first()
            )
            viewModelScope.launch {
                keyValueRepository.get(Keys.timetableVersion(state.school!!.id)).map { it?.toIntOrNull() }.collectLatest { version ->
                    timetableRepository.getTimetableForSchool(state.school!!.id).collect { lessons ->
                        state = state.copy(currentVersion = version, lessons = lessons.filter { state.group in it.groups })
                    }
                }
            }
        }
    }

    fun onEvent(event: HomeUiEvent) {
        viewModelScope.launch {
            when (event) {
                HomeUiEvent.Update -> updateTimetableUseCase(state.school as School.IndiwareSchool)
                is HomeUiEvent.Delete -> {
                    if (event.all) timetableRepository.deleteAllTimetables()
                }
                HomeUiEvent.SneakIn -> {

                }
            }
        }
    }
}

data class HomeUiState(
    val school: School? = null,
    val group: Group? = null,
    val currentVersion: Int? = null,
    val lessons: List<Lesson.TimetableLesson> = emptyList()
)

sealed class HomeUiEvent {
    data object Update : HomeUiEvent()
    data class Delete(val all: Boolean) : HomeUiEvent()
    data object SneakIn : HomeUiEvent()
}