package plus.vplan.app.domain.cache

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.produceState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import plus.vplan.app.core.model.AliasState
import plus.vplan.app.core.model.AliasedItem
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.Item

@Composable
fun <T: Item<*, *>> Flow<CacheState<T>>.collectAsLoadingStateOld(id: String = "Unbekannt") = this.collectAsState(CacheState.Loading(id))

@Composable
fun <T: Item<*, *>> Flow<CacheState<T>>.collectAsResultingFlowOld() = this.filterIsInstance<CacheState.Done<T>>().map { it.data }.collectAsState(null)

@Composable
inline fun <reified T: Item<*, *>> List<Flow<CacheState<T>>>.collectAsResultingFlowOld(): androidx.compose.runtime.State<List<T>> = if (this.isEmpty()) produceState(emptyList()) {} else (combine(this.map { it.filterIsInstance<CacheState.Done<T>>().map { it.data } }) { it.toList() }.collectAsState(emptyList()))

@Composable
inline fun <reified T: Item<*, *>> Flow<List<CacheState<T>>>.collectAsSingleFlowOld(): androidx.compose.runtime.State<List<T>> = this.map { it.filterIsInstance<CacheState.Done<T>>().map { it.data } }.distinctUntilChanged().collectAsState(emptyList())

@Composable
fun <T: AliasedItem<*>> Flow<AliasState<T>>.collectAsLoadingState(id: String = "Unbekannt") = this.collectAsState(AliasState.Loading(id))

@Composable
fun <T: AliasedItem<*>> Flow<AliasState<T>>.collectAsResultingFlow() = this.filterIsInstance<AliasState.Done<T>>().map { it.data }.collectAsState(null)

@Composable
inline fun <reified T: AliasedItem<*>> List<Flow<AliasState<T>>>.collectAsResultingFlow(): androidx.compose.runtime.State<List<T>> = if (this.isEmpty()) produceState(emptyList()) {} else (combine(this.map { it.filterIsInstance<AliasState.Done<T>>().map { it.data } }) { it.toList() }.collectAsState(emptyList()))

@Composable
inline fun <reified T: AliasedItem<*>> Flow<List<AliasState<T>>>.collectAsSingleFlow(): androidx.compose.runtime.State<List<T>> = this.map { it.filterIsInstance<AliasState.Done<T>>().map { it.data } }.distinctUntilChanged().collectAsState(emptyList())