package plus.vplan.app.domain.source

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.LocalFileRepository
import plus.vplan.app.utils.getBitmapFromBytes

class FileSource(
    private val fileRepository: FileRepository,
    private val localFileRepository: LocalFileRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheStateOld<File>>>()
    private val bitmapCache = hashMapOf<Int, ImageBitmap?>()

    fun getById(id: Int): Flow<CacheStateOld<File>> {
        return flows.getOrPut(id) {
            val fileFlow = fileRepository.getById(id, forceReload = false).map {
                if (it is CacheStateOld.Done<File>) {
                    it.copy(
                        data = it.data.copy(
                            getBitmap = {
                                bitmapCache[it.data.id] ?: localFileRepository.getFile("./homework_files/${it.data.id}")?.let { bytes ->
                                    val bitmap = getBitmapFromBytes(bytes)
                                    bitmapCache[it.data.id] = bitmap
                                    bitmap
                                }
                            }
                        )
                    )
                } else it
            }
            val flow = MutableSharedFlow<CacheStateOld<File>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                fileFlow.collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }
}