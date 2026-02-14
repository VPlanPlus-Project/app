package plus.vplan.app.feature.news.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.feature.news.domain.usecase.GetNewsUseCase
import plus.vplan.app.feature.news.domain.usecase.NewsState
import plus.vplan.app.feature.news.domain.usecase.SetNewsAsReadUseCase

class NewsViewModel : ViewModel(), KoinComponent {
    private val setNewsAsReadUseCase by inject<SetNewsAsReadUseCase>()
    private val getNewsUseCase by inject<GetNewsUseCase>()

    val state: StateFlow<NewsState?>
        field = MutableStateFlow(null)


    private var uiSyncContext: Job? = null

    fun init(newsId: Int) {
        uiSyncContext?.cancel()
        uiSyncContext = viewModelScope.launch {
            setNewsAsReadUseCase(newsId)
            getNewsUseCase(newsId).collectLatest { newsState ->
                state.update { newsState }
            }
        }
    }
}
