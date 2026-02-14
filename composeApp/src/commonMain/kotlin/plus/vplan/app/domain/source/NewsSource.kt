package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.domain.model.News
import plus.vplan.app.domain.repository.NewsRepository

class NewsSource(
    private val newsRepository: NewsRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheState<News>>>()

    fun getById(id: Int, forceReload: Boolean = false): Flow<CacheState<News>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<News>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                newsRepository.getById(id, forceReload)
                    .collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }
}