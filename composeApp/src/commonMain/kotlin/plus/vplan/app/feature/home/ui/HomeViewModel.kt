package plus.vplan.app.feature.home.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.LessonTime
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.LessonTimeRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateLessonTimesUseCase

class HomeViewModel(
    private val schoolRepository: SchoolRepository,
    private val groupRepository: GroupRepository,
    private val lessonTimeRepository: LessonTimeRepository,
    private val updateLessonTimesUseCase: UpdateLessonTimesUseCase
) : ViewModel() {
    var state by mutableStateOf(HomeUiState())
        private set

    init {
        viewModelScope.launch {
            state = state.copy(
                school = schoolRepository.getById(67).first(),
                group = groupRepository.getById(1721).first()
            )
            lessonTimeRepository.getByGroup(1721).collect { lessonTimes ->
                state = state.copy(lessonTimes = lessonTimes.filter { !it.interpolated }.sortedBy { it.lessonNumber })
            }
        }
    }

    fun onEvent(event: HomeUiEvent) {
        viewModelScope.launch {
            when (event) {
                HomeUiEvent.Update -> updateLessonTimesUseCase(state.school as School.IndiwareSchool)
                is HomeUiEvent.Delete -> {
                    if (event.all) lessonTimeRepository.deleteById(state.lessonTimes.map { it.id })
                    else lessonTimeRepository.deleteById(state.lessonTimes.filter { it.lessonNumber % 2 == 0 }.map { it.id })
                }
                HomeUiEvent.SneakWeekIn -> {
                    lessonTimeRepository.upsert(LessonTime(
                        id = "idk",
                        start = LocalTime.parse("10:00"),
                        end = LocalTime.parse("11:00"),
                        lessonNumber = 1,
                        group = state.group!!,
                        interpolated = false
                    ))
                }
            }
        }
    }
}

data class HomeUiState(
    val school: School? = null,
    val group: Group? = null,
    val lessonTimes: List<LessonTime> = emptyList()
)

sealed class HomeUiEvent {
    data object Update : HomeUiEvent()
    data class Delete(val all: Boolean) : HomeUiEvent()
    data object SneakWeekIn : HomeUiEvent()
}