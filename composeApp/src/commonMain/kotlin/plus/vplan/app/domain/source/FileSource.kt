package plus.vplan.app.domain.source

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.File
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.LocalFileRepository

class FileSource(
    private val fileRepository: FileRepository,
    private val localFileRepository: LocalFileRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheState<File>>>()
    private val bitmapCache = hashMapOf<Int, ImageBitmap?>()

    fun getById(id: Int): Flow<CacheState<File>> {
        return flows.getOrPut(id) {
            val fileFlow = fileRepository.getById(id, forceReload = false)
            val flow = MutableSharedFlow<CacheState<File>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                fileFlow.collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }
}