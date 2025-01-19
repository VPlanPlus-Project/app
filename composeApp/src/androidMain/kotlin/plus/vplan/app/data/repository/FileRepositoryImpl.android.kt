package plus.vplan.app.data.repository

import android.content.Context
import plus.vplan.app.domain.repository.LocalFileRepository

actual class LocalFileRepositoryImpl(private val context: Context) : LocalFileRepository {
    override suspend fun writeFile(path: String, content: ByteArray) {
        context.filesDir.resolve(path).parentFile?.mkdirs()
        context.filesDir.resolve(path).writeBytes(content)
    }

    override suspend fun deleteFile(path: String) {
        context.filesDir.resolve(path).delete()
    }

    override suspend fun getFile(path: String): ByteArray? {
        try {
            return context.filesDir.resolve(path).readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}