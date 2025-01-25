package plus.vplan.app.feature.assessment.ui.components.create

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase

class NewAssessmentViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase
) : ViewModel() {
    var state by mutableStateOf(NewAssessmentState())
        private set

    init {
        viewModelScope.launch {
            getCurrentProfileUseCase().collectLatest { profile ->
                state = state.copy(currentProfile = (profile as? Profile.StudentProfile).also {
                    it?.getGroupItem()
                })
            }
        }
    }
}

data class NewAssessmentState(
    val currentProfile: Profile.StudentProfile? = null,
    val canShowVppIdBanner: Boolean = false,
    val selectedDefaultLesson: DefaultLesson? = null,
    val selectedDate: LocalDate? = null,
    val description: String = "",
    val isVisible: Boolean? = null
)