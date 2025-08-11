package plus.vplan.app.feature.home.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.BuildConfig
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.News
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.NewsRepository
import plus.vplan.app.utils.now

class GetNewsUseCase(
    private val newsRepository: NewsRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(profile: Profile): Flow<List<News>> {
        val school = profile.getSchool().getFirstValue() ?: return flowOf(emptyList())
        return newsRepository.getAll().mapLatest {
            it.filter { news ->
                (news.schoolIds.isEmpty() || school.id in news.schoolIds) &&
                        LocalDate.now() >= news.dateFrom.toLocalDateTime(TimeZone.currentSystemDefault()).date &&
                        LocalDate.now() <= news.dateTo.toLocalDateTime(TimeZone.currentSystemDefault()).date &&
                        news.versionFrom?.let { BuildConfig.APP_VERSION_CODE >= news.versionFrom } ?: true &&
                        news.versionTo?.let { BuildConfig.APP_VERSION_CODE <= news.versionTo } ?: true
            }
        }.distinctUntilChanged()
    }
}