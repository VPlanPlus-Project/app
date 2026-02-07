package plus.vplan.app.domain.source

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMap
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMapFactory
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.LocalFileRepository
import plus.vplan.app.utils.getBitmapFromBytes

class FileSource : KoinComponent {
    private val fileRepository: FileRepository by inject()
    private val localFileRepository: LocalFileRepository by inject()
    private val concurrentHashMapFactory: ConcurrentHashMapFactory by inject()

    private val flows: ConcurrentHashMap<Int, StateFlow<CacheState<File>>> = concurrentHashMapFactory.create()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val bitmapCache = hashMapOf<Int, ImageBitmap?>()

    fun getById(id: Int): StateFlow<CacheState<File>> {
        return flows.getOrPut(id) {
            fileRepository.getById(id, forceReload = false)
                .map { state ->
                    if (state is CacheState.Done<File>) {
                        state.copy(
                            data = state.data.copy(
                                getBitmap = {
                                    bitmapCache[state.data.id] ?: localFileRepository.getFile("./files/${state.data.id}")?.let { bytes ->
                                        val bitmap = getBitmapFromBytes(bytes)
                                        bitmapCache[state.data.id] = bitmap
                                        bitmap
                                    }
                                }
                            )
                        )
                    } else state
                }
                .stateIn(
                    scope = scope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = CacheState.Loading(id.toString())
                )
        }
    }
}