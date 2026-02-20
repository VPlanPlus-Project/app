@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.data.repository

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.currentConfiguration
import plus.vplan.app.core.database.VppDatabase
import plus.vplan.app.core.database.model.database.DbFile
import plus.vplan.app.data.source.network.GenericAuthenticationProvider
import plus.vplan.app.data.source.network.getAuthenticationOptionsForRestrictedEntity
import plus.vplan.app.data.source.network.model.IncludedModel
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication
import plus.vplan.app.domain.model.data_structure.ConcurrentMutableMap
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.ui.common.AttachedFile
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class FileRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase,
    private val genericAuthenticationProvider: GenericAuthenticationProvider
) : FileRepository {
    private val logger = Logger.withTag("FileRepositoryImpl")

    override suspend fun upsertLocally(
        fileId: Int,
        fileName: String,
        fileSize: Long,
        isOfflineReady: Boolean,
        createdAt: Instant,
        createdBy: Int?
    ) {
        vppDatabase.fileDao.upsert(DbFile(
            id = fileId,
            fileName = fileName,
            createdAt = createdAt,
            createdByVppId = createdBy,
            size = fileSize,
            isOfflineReady = isOfflineReady,
            cachedAt = Clock.System.now()
        ))
    }

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

    private val runningDownloads = ConcurrentMutableMap<Int, Deferred<Response.Error?>>()
    private suspend fun downloadById(id: Int): Response.Error? {
        runningDownloads[id]?.let { return it.await() }

        val deferred = CoroutineScope(Dispatchers.IO).async download@{
            try {
                val authenticationOptions = getAuthenticationOptionsForRestrictedEntity(
                    httpClient = httpClient,
                    url = URLBuilder(currentConfiguration.appApiUrl).apply {
                        appendPathSegments("file", "v1", id.toString())
                    }.buildString()
                )

                if (authenticationOptions is Response.Error.OnlineError.NotFound) {
                    vppDatabase.fileDao.deleteById(listOf(id))
                }

                if (authenticationOptions !is Response.Success) return@download authenticationOptions as Response.Error
                val authentication = genericAuthenticationProvider.getAuthentication(authenticationOptions.data)

                if (authentication == null) {
                    return@download Response.Error.Other("No authentication found for file $id")
                }

                val response = httpClient.get {
                    url(URLBuilder(currentConfiguration.appApiUrl).apply {
                        appendPathSegments("file", "v1", id.toString())
                    }.build())
                    authentication.authentication(this)
                }

                if (!response.status.isSuccess()) {
                    logger.e { "Error downloading file data with id $id: $response" }
                    return@download response.toErrorResponse()
                }

                val file = response.body<ResponseDataWrapper<FileItemGetResponse>>().data

                val existing = vppDatabase.fileDao.getById(id).first()?.toModel()
                upsertLocally(
                    fileId = id,
                    fileName = file.fileName,
                    fileSize = file.size,
                    isOfflineReady = existing?.isOfflineReady ?: false,
                    createdAt = Instant.fromEpochSeconds(file.createdAt),
                    createdBy = file.createdBy.id
                )

                return@download null
            } finally {
                runningDownloads.remove(id)
            }
        }

        runningDownloads[id] = deferred
        return deferred.await()
    }

    private val idFileFlowCache = mutableMapOf<String, Flow<CacheState<File>>>()
    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<File>> {
        val cacheKey = "${id}_$forceReload"
        if (!forceReload) idFileFlowCache[cacheKey]?.let { return it }

        val flow = flow {
            var hasReloaded = false
            vppDatabase.fileDao.getById(id).map { it?.toModel() }.collect { file ->
                if (file == null || (forceReload && !hasReloaded)) {
                    hasReloaded = true
                    emit(CacheState.Loading(id.toString()))
                    val downloadError = downloadById(id)
                    if (downloadError != null) {
                        if (downloadError is Response.Error.OnlineError.NotFound) emit(CacheState.NotExisting(id.toString()))
                        else emit(CacheState.Error(id.toString(), downloadError))
                        return@collect
                    }
                }
                file?.let { emit(CacheState.Done(it)) }
            }
        }
            .onCompletion { idFileFlowCache.remove(cacheKey) }
            .shareIn(CoroutineScope(Dispatchers.IO), replay = 1, started = SharingStarted.WhileSubscribed(5000))

        if (!forceReload) idFileFlowCache[cacheKey] = flow
        return flow
    }

    override fun getAllIds(): Flow<List<Int>> {
        return vppDatabase.fileDao.getAll().map { it.map { file -> file.id } }
    }

    override fun downloadFileContent(file: File, schoolApiAccess: VppSchoolAuthentication): Flow<FileDownloadProgress> = channelFlow {
        safeRequest(onError = { trySend(FileDownloadProgress.Error(it)) }) {
            val response = httpClient.get {
                url(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("file", "v1", file.id.toString(), "download")
                }.build())
                schoolApiAccess.authentication(this)
                onDownload { bytesSentTotal, contentLength ->
                    if (contentLength == null) send(FileDownloadProgress.InProgress(0f))
                    else send(FileDownloadProgress.InProgress((bytesSentTotal.toFloat() / contentLength.toFloat())))
                }
            }
            if (!response.status.isSuccess()) return@channelFlow send(FileDownloadProgress.Error(response.toErrorResponse()))
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
                url(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("file", "v1")
                }.build())
                vppId.buildVppSchoolAuthentication().authentication(this)
                header("File-Name", document.name)
                header(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
                header(HttpHeaders.ContentLength, document.size.toString())
                setBody(ByteReadChannel(document.platformFile.readBytes()))
            }
            if (response.status != HttpStatusCode.OK) return response.toErrorResponse()
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
                url(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("file", "v1", file.id.toString())
                }.build())

                vppId.buildVppSchoolAuthentication().authentication(this)
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
                url(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("file", "v1", file.id.toString())
                }.build())
                bearerAuth(vppId.accessToken)
            }
            if (!response.status.isSuccess()) return response.toErrorResponse()
            delete()
            return null
        }
        return Response.Error.Cancelled
    }
}

@Serializable
data class FileItemGetResponse(
    @SerialName("created_by") val createdBy: IncludedModel,
    @SerialName("file_name") val fileName: String,
    @SerialName("file_size") val size: Long,
    @SerialName("created_at") val createdAt: Long,
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