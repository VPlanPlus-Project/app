package plus.vplan.app.feature.home.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateSubstitutionPlanUseCase

class HomeViewModel(
    private val schoolRepository: SchoolRepository,
    private val groupRepository: GroupRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val updateSubstitutionPlanUseCase: UpdateSubstitutionPlanUseCase
) : ViewModel() {
    var state by mutableStateOf(HomeUiState())
        private set

    init {
        viewModelScope.launch {
            state = state.copy(
                school = schoolRepository.getById(35).first(),
                group = groupRepository.getById(891).first()
            )
            viewModelScope.launch {
                substitutionPlanRepository.getSubstitutionPlanBySchool(35, LocalDate(2024, 12, 19)).collect {
                    state = state.copy(lessons = it.filter { state.group in it.groups })
                }
            }
        }
    }

    fun onEvent(event: HomeUiEvent) {
        viewModelScope.launch {
            when (event) {
                HomeUiEvent.Update -> updateSubstitutionPlanUseCase(state.school as School.IndiwareSchool, date = LocalDate(2024,12,19))
                is HomeUiEvent.Delete -> {
                    if (event.all) substitutionPlanRepository.deleteAllSubstitutionPlans()
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
    val lessons: List<Lesson.SubstitutionPlanLesson> = emptyList()
)

sealed class HomeUiEvent {
    data object Update : HomeUiEvent()
    data class Delete(val all: Boolean) : HomeUiEvent()
    data object SneakIn : HomeUiEvent()
}