package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.api
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbFile
import plus.vplan.app.data.source.network.isResponseFromBackend
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.ui.common.AttachedFile
import plus.vplan.app.utils.sendAll

class FileRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : FileRepository {
    override suspend fun upsert(file: File) {
        vppDatabase.fileDao.upsert(
            DbFile(
                id = file.id,
                createdAt = Clock.System.now(),
                createdByVppId = null,
                fileName = file.name,
                size = file.size,
                isOfflineReady = file.isOfflineReady,
                cachedAt = file.cachedAt
            )
        )
    }

    override fun getById(id: Int, forceReload: Boolean): Flow<CacheStateOld<File>> {
        val fileFlow = vppDatabase.fileDao.getById(id).map { it?.toModel() }
        return channelFlow {
            if (!forceReload) {
                var hadData = false
                sendAll(fileFlow.takeWhile { it != null }.filterNotNull().onEach { hadData = true }.map { CacheStateOld.Done(it) })
                if (hadData || id < 0) return@channelFlow
            }
            send(CacheStateOld.Loading(id.toString()))
            safeRequest(onError = { trySend(CacheStateOld.Error(id.toString(), it)) }) {
                val response = httpClient.get {
                    url(URLBuilder(api).apply {
                        appendPathSegments("api", "v2.2", "file", id.toString())
                    }.build())
                }
                if (response.status == HttpStatusCode.NotFound && response.isResponseFromBackend()) {
                    vppDatabase.fileDao.deleteById(listOf(id))
                    return@channelFlow send(CacheStateOld.NotExisting(id.toString()))
                }
                if (!response.status.isSuccess()) return@channelFlow send(CacheStateOld.Error(id.toString(), response.toErrorResponse<File>()))
                val data = ResponseDataWrapper.fromJson<FileItemSimpleGetRequest>(response.bodyAsText())
                    ?: return@channelFlow send(CacheStateOld.Error(id.toString(), Response.Error.ParsingError(response.bodyAsText())))

                val creator = vppDatabase.vppIdDao.getById(data.createdBy).first()?.toModel() as? VppId.Active
                if (creator != null) {
                    val fileResponse = httpClient.get {
                        url(URLBuilder(api).apply {
                            appendPathSegments("api", "v2.2", "file", id.toString())
                        }.build())
                        creator.buildSchoolApiAccess().authentication(this)
                    }
                    if (!fileResponse.status.isSuccess()) return@channelFlow send(CacheStateOld.Error(id.toString(), fileResponse.toErrorResponse<File>()))
                    val fileData = ResponseDataWrapper.fromJson<FileItemGetRequest>(fileResponse.bodyAsText())
                        ?: return@channelFlow send(CacheStateOld.Error(id.toString(), Response.Error.ParsingError(fileResponse.bodyAsText())))
                    val existing = vppDatabase.fileDao.getById(id).first()?.toModel()
                    vppDatabase.fileDao.upsert(
                        DbFile(
                            id = id,
                            createdAt = Clock.System.now(),
                            createdByVppId = data.createdBy,
                            fileName = fileData.fileName,
                            size = fileData.size,
                            isOfflineReady = existing?.isOfflineReady == true,
                            cachedAt = Clock.System.now()
                        )
                    )
                    return@channelFlow sendAll(getById(id, false))
                }
                val schools = vppDatabase.schoolDao.getAll().first()
                    .map { it.toModel() }
                    .filter { it.getVppSchoolId() != null }
                    .filter { it.getVppSchoolId() in data.schoolIds }
                    .mapNotNull { try { it.getSchoolApiAccess() } catch (_: Exception) { null } }

                schools.forEach { school ->
                    val fileResponse = httpClient.get {
                        url(URLBuilder(api).apply {
                            appendPathSegments("api", "v2.2", "file", id.toString())
                        }.build())
                        school.authentication(this)
                    }
                    if (fileResponse.status == HttpStatusCode.Forbidden) return@forEach
                    if (!fileResponse.status.isSuccess()) return@channelFlow send(CacheStateOld.Error(id.toString(), fileResponse.toErrorResponse<File>()))
                    val fileData = ResponseDataWrapper.fromJson<FileItemGetRequest>(fileResponse.bodyAsText())
                        ?: return@channelFlow send(CacheStateOld.Error(id.toString(), Response.Error.ParsingError(fileResponse.bodyAsText())))
                    val existing = vppDatabase.fileDao.getById(id).first()?.toModel()
                    vppDatabase.fileDao.upsert(
                        DbFile(
                            id = id,
                            createdAt = Clock.System.now(),
                            createdByVppId = data.createdBy,
                            fileName = fileData.fileName,
                            size = fileData.size,
                            isOfflineReady = existing?.isOfflineReady == true,
                            cachedAt = Clock.System.now()
                        )
                    )
                    return@channelFlow sendAll(getById(id, false))
                }
                return@channelFlow send(CacheStateOld.NotExisting(id.toString()))
            }
        }
    }

    override fun getAllIds(): Flow<List<Int>> {
        return vppDatabase.fileDao.getAll().map { it.map { file -> file.id } }
    }

    override fun cacheFile(file: File, schoolApiAccess: SchoolApiAccess): Flow<FileDownloadProgress> = channelFlow {
        safeRequest(onError = { trySend(FileDownloadProgress.Error(it)) }) {
            val response = httpClient.get {
                url(URLBuilder(api).apply {
                    appendPathSegments("api", "v2.2", "file", file.id.toString(), "download")
                }.build())
                schoolApiAccess.authentication(this)
                onDownload { bytesSentTotal, contentLength ->
                    if (contentLength == null) send(FileDownloadProgress.InProgress(0f))
                    else send(FileDownloadProgress.InProgress((bytesSentTotal.toFloat() / contentLength.toFloat())))
                }
            }
            if (!response.status.isSuccess()) return@channelFlow send(FileDownloadProgress.Error(response.toErrorResponse<File>()))
            val data = response.bodyAsBytes()
            return@channelFlow send(FileDownloadProgress.Done(data) {
                vppDatabase.fileDao.setOfflineReady(file.id, true)
            })
        }
    }

    override suspend fun uploadFile(
        vppId: VppId.Active,
        document: AttachedFile
    ): Response<Int> {
        safeRequest(onError = { return it }) {
            val response = httpClient.post {
                url(URLBuilder(api).apply {
                    appendPathSegments("api", "v2.2", "file")
                }.build())
                vppId.buildSchoolApiAccess().authentication(this)
                header("File-Name", document.name)
                header(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
                header(HttpHeaders.ContentLength, document.size.toString())
                setBody(ByteReadChannel(document.platformFile.readBytes()))
            }
            if (response.status != HttpStatusCode.OK) return response.toErrorResponse<Int>()
            return ResponseDataWrapper.fromJson<Int>(response.bodyAsText())?.let { Response.Success(it) } ?: Response.Error.ParsingError(response.bodyAsText())
        }
        return Response.Error.Cancelled
    }

    override suspend fun getMinIdForLocalFile(): Int {
        return (vppDatabase.fileDao.getLocalMinId() ?: -1).coerceAtMost(-1)
    }

    override suspend fun setOfflineReady(file: File, isOfflineReady: Boolean) {
        vppDatabase.fileDao.setOfflineReady(file.id, isOfflineReady)
    }

    override suspend fun renameFile(file: File, newName: String, vppId: VppId.Active?) {
        val oldName = file.name
        vppDatabase.fileDao.updateName(file.id, newName)
        if (file.id < 0 || vppId == null) return
        safeRequest(onError = { vppDatabase.fileDao.updateName(file.id, oldName) }) {
            val response = httpClient.patch {
                url(URLBuilder(api).apply {
                    appendPathSegments("api", "v2.2", "file", file.id.toString())
                }.build())

                bearerAuth(vppId.accessToken)
                contentType(ContentType.Application.Json)
                setBody(FileUpdateNameRequest(newName))
            }
            if (!response.status.isSuccess()) vppDatabase.fileDao.updateName(file.id, oldName)
        }
    }

    override suspend fun deleteFile(file: File, vppId: VppId.Active?): Response.Error? {
        val delete: suspend () -> Unit = {
            vppDatabase.fileDao.deleteById(file.id)
            vppDatabase.fileDao.deleteHomeworkFileConnections(file.id)
        }
        if (file.id < 0 || vppId == null) {
            delete()
            return null
        }
        safeRequest(onError = { return it }) {
            val response = httpClient.delete {
                url(URLBuilder(api).apply {
                    appendPathSegments("api", "v2.2", "file", file.id.toString())
                }.build())
                bearerAuth(vppId.accessToken)
            }
            if (!response.status.isSuccess()) return response.toErrorResponse<Any>()
            delete()
            return null
        }
        return Response.Error.Cancelled
    }
}

@Serializable
data class FileItemSimpleGetRequest(
    @SerialName("created_by") val createdBy: Int,
    @SerialName("school_ids") val schoolIds: List<Int>,
)

@Serializable
data class FileItemGetRequest(
    @SerialName("created_by") val createdBy: Int,
    @SerialName("file_name") val fileName: String,
    @SerialName("size") val size: Long,
)

@Serializable
data class FileUpdateNameRequest(
    @SerialName("file_name") val fileName: String,
)

sealed class FileDownloadProgress {
    data class InProgress(val progress: Float) : FileDownloadProgress()
    data class Error(val error: Response.Error) : FileDownloadProgress()
    class Done(val content: ByteArray, val onFileSaved: suspend () -> Unit) : FileDownloadProgress()
}