package plus.vplan.app.feature.home.domain.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.News
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.NewsRepository
import kotlin.coroutines.coroutineContext

class GetNewsUseCase(
    private val newsRepository: NewsRepository
) {
    operator fun invoke(profile: Profile): Flow<List<News>> = flow<List<News>> {
        while (coroutineContext.isActive) {
            newsRepository.getBySchool(profile.getSchool().getFirstValue()?.getSchoolApiAccess() ?: return@flow, false).let {
                if (it !is Response.Success) emit(emptyList())
                else emit(it.data.filter { it.schoolIds.isEmpty() || profile.getSchool().getFirstValue()!!.id in it.schoolIds })
            }
            delay(100)
        }
    }.distinctUntilChanged()
}