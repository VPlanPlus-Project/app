package plus.vplan.app.feature.homework.ui.components.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Homework

class DetailViewModel : ViewModel() {
    var state by mutableStateOf(DetailState())
        private set

    fun init(homeworkId: Int) {
        viewModelScope.launch {
            App.homeworkSource.getById(homeworkId).collectLatest {
                if (it is CacheState.Done) {
                    it.data.getDefaultLessonItem()
                    it.data.getGroupItem()
                    it.data.getTaskItems()
                    if (it.data is Homework.CloudHomework) it.data.getCreatedBy()
                    state = state.copy(homework = it.data)
                }
            }
        }
    }
}

data class DetailState(
    val homework: Homework? = null
)