package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.repository.FileDownloadProgress
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.model.SchoolApiAccess

interface FileRepository {
    fun getById(id: Int): Flow<CacheState<File>>
    fun cacheFile(file: File, schoolApiAccess: SchoolApiAccess): Flow<FileDownloadProgress>
}