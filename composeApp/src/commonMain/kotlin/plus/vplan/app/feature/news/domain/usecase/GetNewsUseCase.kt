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
import plus.vplan.app.core.data.news.NewsRepository
import plus.vplan.app.core.data.school.SchoolRepository
import plus.vplan.app.core.model.News

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

                newsRepository.getById(newsId)
                    .filterNotNull()
                    .collectLatest { newsState ->
                        val schoolNames =
                            if (newsState.schools.isEmpty()) NewsSchoolsState.All
                            else newsState.schools
                                .mapNotNull { schoolId -> schoolRepository.getById(schoolId).first()?.name }
                                .distinct()
                                .let { NewsSchoolsState.Ready(it) }

                        flowState.update {
                            NewsState(
                                news = newsState,
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