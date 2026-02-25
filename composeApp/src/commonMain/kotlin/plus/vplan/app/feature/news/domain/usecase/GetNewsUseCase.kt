package plus.vplan.app.feature.news.domain.usecase

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.data.school.SchoolRepository
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.News
import plus.vplan.app.domain.repository.NewsRepository

class GetNewsUseCase : KoinComponent {
    private val newsRepository by inject<NewsRepository>()
    private val schoolRepository by inject<SchoolRepository>()

    operator fun invoke(newsId: Int): Flow<NewsState> {
        val flowState = MutableStateFlow<NewsState?>(null)
        return flow {
            coroutineScope {
                flowState
                    .filterNotNull()
                    .onEach(::emit)
                    .launchIn(this)

                newsRepository.getById(newsId, false)
                    .collectLatest { newsState ->
                        if (newsState !is CacheState.Done) return@collectLatest // This should not happen as a news item can only be opened if it is already loaded

                        val schoolNames =
                            if (newsState.data.schools.isEmpty()) NewsSchoolsState.All
                            else newsState.data.schools
                                .mapNotNull { schoolId -> schoolRepository.getById(schoolId).first()?.name }
                                .distinct()
                                .let { NewsSchoolsState.Ready(it) }

                        flowState.update {
                            NewsState(
                                news = newsState.data,
                                schoolNames = schoolNames
                            )
                        }
                    }
            }
        }
    }
}

data class NewsState(
    val news: News,
    val schoolNames: NewsSchoolsState
)

sealed class NewsSchoolsState {
    data object All : NewsSchoolsState()
    data class Ready(val names: List<String>) : NewsSchoolsState()
}