package plus.vplan.app.data.repository

import plus.vplan.app.domain.repository.FileRepository

actual class FileRepositoryImpl : FileRepository {
    override suspend fun writeFile(path: String, content: ByteArray) {
        TODO("Not yet implemented")
    }
}