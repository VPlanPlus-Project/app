package plus.vplan.app.domain.repository

interface LocalFileRepository {
    suspend fun writeFile(path: String, content: ByteArray)
    suspend fun getFile(path: String): ByteArray?
}