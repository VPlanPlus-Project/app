package plus.vplan.app.domain.source

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.LocalFileRepository
import plus.vplan.app.utils.getBitmapFromBytes

class FileSource(
    private val fileRepository: FileRepository,
    private val localFileRepository: LocalFileRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheState<File>>>()
    private val bitmapCache = hashMapOf<Int, ImageBitmap?>()

    fun getById(id: Int): Flow<CacheState<File>> {
        return flows.getOrPut(id) {
            val fileFlow = fileRepository.getById(id, forceReload = false).map {
                if (it is CacheState.Done<File>) {
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
            val flow = MutableSharedFlow<CacheState<File>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            MainScope().launch {
                fileFlow.collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }
}