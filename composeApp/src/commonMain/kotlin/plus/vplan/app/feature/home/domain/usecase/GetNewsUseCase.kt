package plus.vplan.app.feature.home.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.AppBuildConfig
import plus.vplan.app.domain.model.News
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.NewsRepository
import plus.vplan.app.utils.now

class GetNewsUseCase(
    private val newsRepository: NewsRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(profile: Profile): Flow<List<News>> {
        return newsRepository.getAll().mapLatest {
            it.filter { news ->
                (news.schools.isEmpty() || profile.school.id in news.schools.map { it.id }) &&
                        LocalDate.now() >= news.dateFrom.toLocalDateTime(TimeZone.currentSystemDefault()).date &&
                        LocalDate.now() <= news.dateTo.toLocalDateTime(TimeZone.currentSystemDefault()).date &&
                        news.versionFrom?.let { AppBuildConfig.APP_VERSION_CODE >= news.versionFrom } ?: true &&
                        news.versionTo?.let { AppBuildConfig.APP_VERSION_CODE <= news.versionTo } ?: true
            }
        }.distinctUntilChanged()
    }
}