package plus.vplan.app.data.repository

import android.content.Context
import plus.vplan.app.domain.repository.FileRepository

actual class FileRepositoryImpl(private val context: Context) : FileRepository {
    override suspend fun writeFile(path: String, content: ByteArray) {
        context.filesDir.resolve(path).parentFile?.mkdirs()
        context.filesDir.resolve(path).writeBytes(content)
    }
}