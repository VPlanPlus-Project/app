package plus.vplan.app.domain.cache

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import plus.vplan.app.core.model.AliasState
import plus.vplan.app.core.model.AliasedItem

@Composable
fun <T: AliasedItem<*>> Flow<AliasState<T>>.collectAsLoadingState(id: String = "Unbekannt") = this.collectAsState(AliasState.Loading(id))

@Composable
fun <T: AliasedItem<*>> Flow<AliasState<T>>.collectAsResultingFlow() = this.filterIsInstance<AliasState.Done<T>>().map { it.data }.collectAsState(null)
