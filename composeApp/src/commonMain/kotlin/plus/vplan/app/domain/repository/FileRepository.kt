package plus.vplan.app.domain.repository

interface FileRepository {
    suspend fun writeFile(path: String, content: ByteArray)
}