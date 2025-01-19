package plus.vplan.app.domain.source

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.LocalFileRepository
import plus.vplan.app.utils.getBitmapFromBytes

class FileSource(
    private val fileRepository: FileRepository,
    private val localFileRepository: LocalFileRepository
) {
    private val cache = hashMapOf<Int, Flow<CacheState<File>>>()
    private val bitmapCache = hashMapOf<Int, ImageBitmap?>()

    fun getById(id: Int): Flow<CacheState<File>> {
        return cache.getOrPut(id) { fileRepository.getById(id).map {
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
        } }
    }
}