package plus.vplan.app.domain.cache

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.data.Response

sealed class CacheState<out T: Item>(val entityId: String) {
    data class Loading(val id: String): CacheState<Nothing>(id)
    data class NotExisting(val id: String): CacheState<Nothing>(id)
    data class Error(val id: String, val error: Response.Error): CacheState<Nothing>(id)
    data class Done<T: Item>(val data: T): CacheState<T>(data.getEntityId())
}

interface Item {
    fun getEntityId(): String
}

suspend fun <T: Item> Flow<CacheState<T>>.getFirstValue() = this.filterIsInstance<CacheState.Done<T>>().first().data
@Composable
fun <T: Item> Flow<CacheState<T>>.collectAsLoadingState(id: String) = this.collectAsState(CacheState.Loading(id))