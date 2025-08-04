package plus.vplan.app.feature.news.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.News
import plus.vplan.app.feature.news.domain.usecase.SetNewsAsReadUseCase

class NewsViewModel(
    private val setNewsAsReadUseCase: SetNewsAsReadUseCase
) : ViewModel() {
    var state by mutableStateOf(NewsState())
        private set

    private var uiSyncContext: Job? = null

    fun init(newsId: Int) {
        uiSyncContext?.cancel()
        uiSyncContext = viewModelScope.launch {
            setNewsAsReadUseCase(newsId)
            App.newsSource.getById(newsId).filterIsInstance<CacheState.Done<News>>().map { it.data }.collectLatest {
                state = state.copy(news = it)
            }
        }
    }
}

data class NewsState(
    val news: News? = null
)