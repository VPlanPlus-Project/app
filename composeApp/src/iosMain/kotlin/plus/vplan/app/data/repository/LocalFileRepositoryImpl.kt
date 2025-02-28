package plus.vplan.app.data.repository

import plus.vplan.app.domain.repository.LocalFileRepository

class LocalFileRepositoryImpl : LocalFileRepository {
    override suspend fun writeFile(path: String, content: ByteArray) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteFile(path: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getFile(path: String): ByteArray? {
        TODO("Not yet implemented")
    }
}